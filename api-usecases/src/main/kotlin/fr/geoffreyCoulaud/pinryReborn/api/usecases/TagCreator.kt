package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Tag
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.TagRepositoryInterface
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID.randomUUID

@ApplicationScoped
class TagCreator(
    private val tagRepository: TagRepositoryInterface,
) {
    fun findOrCreate(name: String): Tag =
        tagRepository.findTagByName(name)
            ?: tagRepository.saveTag(Tag(id = randomUUID(), name = name))
}
