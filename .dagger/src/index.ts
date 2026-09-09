/**
 * Pinry Reborn's pipeline.
 *
 * One gate, called the same way on a workstation and on a runner
 * (docs/adr/0024-three-projects-share-one-repository.md, decision 5).
 */
import {
  dag,
  CacheSharingMode,
  Container,
  Directory,
  Platform,
  ReturnType,
  argument,
  func,
  object,
} from "@dagger.io/dagger"

/**
 * What the pipeline never needs from the working tree. `.git` is deliberately kept: the
 * prose rule reads the index to know which files are tracked.
 */
const IGNORE = ["**/build", "**/.gradle", "**/.kotlin", ".dagger/sdk", "**/node_modules"]

/**
 * The two long dashes the prose rule refuses, built from code points so that this file is
 * not its own offender.
 */
const LONG_DASHES = [String.fromCodePoint(0x2014), String.fromCodePoint(0x2013)]

/** Dated documents are frozen once delivered, so the dashes they carry stay where they are. */
const FROZEN = [":!docs/specs", ":!docs/plans", ":!docs/adr", ":!docs/handoffs"]

/**
 * Gradle otherwise sizes its worker pool from the container's core count, which is the engine
 * host's and not the runner's. Pinned to a runner's four so the build has one shape everywhere.
 */
const MAX_WORKERS = "--max-workers=4"

/** The build that emits `contract/openapi.json` and the fast jar the image ships. It always runs
 * here: the container starts with no build output, so nothing is ever up to date. */
const QUARKUS_BUILD = ":api-application:quarkusBuild"

/** The fast-jar layout, under `api/`. It is the whole of the image's build context. */
const FAST_JAR = "api-application/build/quarkus-app"

/** The port the runtime image serves on. */
const HTTP_PORT = 8080

/** How long the smoke test gives the container to answer, in seconds. */
const SMOKE_SECONDS = 60

/**
 * The smoke test's body. `\${i}` is the shell's variable and not this file's: escaping it is what
 * keeps the message honest about which second answered.
 */
const POLL = `
for i in $(seq 1 ${SMOKE_SECONDS}); do
  if body=$(curl -fsS http://api:${HTTP_PORT}/q/health); then
    echo "healthy after \${i}s: $body"
    exit 0
  fi
  sleep 1
done
echo "no answer on /q/health within ${SMOKE_SECONDS}s" >&2
exit 1
`

@object()
export class PinryReborn {
  /**
   * The whole gate: the Gradle gate, the repository's prose rules and the contract's synchronisation.
   */
  @func()
  async gate(
    @argument({ defaultPath: "/", ignore: IGNORE }) source: Directory,
  ): Promise<string> {
    // One Gradle invocation for both parts. Two would serialize on the shared cache volume and
    // the second would recompile what the first had just compiled.
    const built = this.gradleRun(source, "gate", QUARKUS_BUILD)
    // The contract is read after the build, not beside it. Asking for both at once makes two
    // requests for one container, and the second waits on a cache volume the first holds.
    const [api, prose] = await Promise.all([built.stdout(), this.prose(source)])
    const contract = await this.contractIsSynchronised(built.directory("/src/contract"), source)
    return [api, prose, contract].join("\n")
  }

  /**
   * The Gradle gate alone, in a container pinning the JDK 25 toolchain and libvips.
   */
  @func()
  apiGate(
    @argument({ defaultPath: "/", ignore: IGNORE }) source: Directory,
  ): Container {
    return this.gradleRun(source, "gate")
  }

  /**
   * The generated contract directory, so a caller can compare it or export it.
   */
  @func()
  contract(
    @argument({ defaultPath: "/", ignore: IGNORE }) source: Directory,
  ): Directory {
    return this.gradleRun(source, QUARKUS_BUILD).directory("/src/contract")
  }

  /**
   * The rules whose scope is the repository rather than one ecosystem: no long dash in a
   * tracked text file, and the evidence guard's own tests.
   */
  @func()
  async prose(
    @argument({ defaultPath: "/", ignore: IGNORE }) source: Directory,
  ): Promise<string> {
    const base = this.repository(source)
    // `git grep` answers by its exit status: 0 found something, 1 found nothing, above that it failed.
    const dashes = base.withExec(
      ["git", "grep", "-n", "-I", "-F", ...LONG_DASHES.flatMap((d) => ["-e", d]), "--", ".", ...FROZEN],
      { expect: ReturnType.Any },
    )
    const guard = base.withExec([
      "python3",
      "-m",
      "unittest",
      "discover",
      "--start-directory",
      ".claude/hooks",
    ])
    const [status, offenders, failure, guardReport] = await Promise.all([
      dashes.exitCode(),
      dashes.stdout(),
      dashes.stderr(),
      guard.stderr(),
    ])
    if (status === 0) {
      throw new Error(
        "Em dash or en dash found. Use a colon, a period, parentheses or a hyphen:\n" + offenders,
      )
    }
    if (status !== 1) {
      throw new Error(`The long dash search failed (exit ${status}):\n${failure}`)
    }
    return `no long dash in a tracked text file\n${guardReport.trim()}`
  }

  /**
   * The Quarkus fast-jar layout the `Dockerfile` copies, so a caller can build the image with
   * no JDK of its own. The same build produces the contract.
   */
  @func()
  quarkusApp(
    @argument({ defaultPath: "/", ignore: IGNORE }) source: Directory,
  ): Directory {
    return this.gradleRun(source, QUARKUS_BUILD).directory(`/src/api/${FAST_JAR}`)
  }

  /**
   * The runtime image. Each line is read from inside the image that was built, not from the
   * request that asked for it, so a run that built one architecture cannot report two.
   *
   * @param platforms What to build. Empty means the engine's own, which is what a pull request
   * needs: the other is emulated, costs minutes, and `buildx` builds both on the release path
   * anyway. `--platforms=linux/amd64,linux/arm64` asks for everything the image ships on.
   */
  @func()
  async image(
    @argument({ defaultPath: "/", ignore: IGNORE }) source: Directory,
    platforms: Platform[] = [],
  ): Promise<string> {
    const wanted = platforms.length > 0 ? platforms : [await dag.defaultPlatform()]
    const context = this.imageContext(source)
    const lines = await Promise.all(
      wanted.map(async (platform) => {
        const machine = await context.dockerBuild({ platform }).withExec(["uname", "-m"]).stdout()
        return `${platform}: built, ${machine.trim()} inside`
      }),
    )
    return lines.join("\n")
  }

  /**
   * The image starts and reports healthy. The test suite never reads production's
   * `application.properties`, its own sharing that name and winning by classpath order, so this
   * is the only thing in the repository that starts what ships.
   */
  @func()
  async smoke(
    @argument({ defaultPath: "/", ignore: IGNORE }) source: Directory,
  ): Promise<string> {
    // The engine's own platform: an emulated container would measure the emulator. Named rather
    // than defaulted, so this is the variant `image` built and not a second build of it.
    const platform = await dag.defaultPlatform()
    const runtime = this.imageContext(source).dockerBuild({ platform })
    const service = runtime.withExposedPort(HTTP_PORT).asService({ useEntrypoint: true })
    // The image carries curl for its own HEALTHCHECK, so it is its own prober.
    const probe = runtime
      .withServiceBinding("api", service)
      .withExec(["sh", "-c", POLL], { expect: ReturnType.Any })
    const [status, report, failure] = await Promise.all([
      probe.exitCode(),
      probe.stdout(),
      probe.stderr(),
    ])
    if (status !== 0) {
      throw new Error(`The image never reported healthy (exit ${status}):\n${failure}`)
    }
    return report.trim()
  }

  /**
   * The `Dockerfile` and the one directory it copies, and nothing else. A context built from
   * exactly what the image needs keys the build on the artefact instead of on the working tree.
   */
  private imageContext(source: Directory): Directory {
    return dag
      .directory()
      .withFile("Dockerfile", source.file("api/Dockerfile"))
      .withDirectory(FAST_JAR, this.quarkusApp(source))
  }

  /**
   * The committed contract is what the build produces, or the gate names the command that
   * makes it so. The pre-commit hook used to rewrite it mid-commit; this refuses instead.
   *
   * The command names the augmentation task and `--rerun`, not `quarkusBuild`: a document
   * edited by hand leaves every source untouched, so an ordinary build is up to date and
   * writes nothing, and the developer would run the command and see no change.
   */
  private async contractIsSynchronised(
    produced: Directory,
    source: Directory,
  ): Promise<string> {
    const [generated, committed] = await Promise.all([
      produced.file("openapi.json").contents(),
      source.file("contract/openapi.json").contents(),
    ])
    if (generated !== committed) {
      throw new Error(
        "contract/openapi.json is not what the build produces. Regenerate and commit it:\n" +
          "  cd api && ./gradlew :api-application:quarkusAppPartsBuild --rerun",
      )
    }
    return "contract/openapi.json is synchronised"
  }

  /** The build, asked for one set of tasks. Every Gradle call in this module goes through here. */
  private gradleRun(source: Directory, ...tasks: string[]): Container {
    return this.gradle(source).withExec(["./gradlew", ...tasks, "--no-daemon", MAX_WORKERS])
  }

  /**
   * The Gradle environment. The JDK is the toolchain the build asks for, so Gradle adopts it
   * instead of provisioning one; libvips is what vips-ffm loads, under the t64 name Ubuntu
   * gives it after the 64-bit time_t transition.
   */
  private gradle(source: Directory): Container {
    return dag
      .container()
      .from("eclipse-temurin:25-jdk")
      .withExec(["apt-get", "update"])
      .withExec(["apt-get", "install", "-y", "--no-install-recommends", "libvips42t64"])
      // One volume, locked. Gradle takes exclusive file locks inside its home, so two
      // invocations sharing it make one fail on the journal lock; locked serializes them
      // instead. One volume and not two, because two locks taken in either order deadlock.
      .withMountedCache("/root/.gradle", dag.cacheVolume("gradle-home"), {
        sharing: CacheSharingMode.Locked,
      })
      .withMountedDirectory("/src", source)
      .withWorkdir("/src/api")
  }

  /** The repository-wide environment: git for the tracked file list, python3 for the guard's tests. */
  private repository(source: Directory): Container {
    return dag
      .container()
      .from("debian:trixie-slim")
      .withExec(["apt-get", "update"])
      .withExec(["apt-get", "install", "-y", "--no-install-recommends", "git", "python3"])
      .withMountedDirectory("/src", source)
      .withWorkdir("/src")
  }
}
