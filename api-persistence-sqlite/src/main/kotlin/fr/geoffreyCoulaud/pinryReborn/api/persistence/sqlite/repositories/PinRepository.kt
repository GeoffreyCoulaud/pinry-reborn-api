package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.repositories

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.PinRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.PinModelMapper.toDomain
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.PinModelMapper.toModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.PinModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.query.QPinModel
import io.ebean.Database
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class PinRepository(
    private val database: Database,
) : PinRepositoryInterface {
    private val sqlRepository = ModelRepository(entityClass = PinModel::class, database = database)

    override fun savePin(pin: Pin): Pin =
        sqlRepository
            .saveAndReturn(pin.toModel())
            .toDomain()

    override fun findPinById(id: UUID): Pin? =
        QPinModel()
            .id
            .equalTo(id)
            .findOne()
            ?.toDomain()

    override fun findAllUserPins(user: User): List<Pin> =
        QPinModel()
            .author.id
            .equalTo(user.id)
            .findList()
            .map { it.toDomain() }
}
