package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.repositories

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Page
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Tag
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PaginationDirection
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PinSortStrategy
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.PinRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.PinModelMapper.toDomain
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.PinModelMapper.toModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.TagModelMapper.toDomain
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.TagModelMapper.toModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.PinModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.PinTagModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.query.QPinModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.query.QPinTagModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.pagination.Cursor
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.pagination.ModelPaginationHelper
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.pagination.PinModelSortStrategy
import io.ebean.Database
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class PinRepository(
    private val database: Database,
) : PinRepositoryInterface {
    private val sqlRepository = ModelRepository(
        entityClass = PinModel::class,
        database = database
    )
    private val pinModelPaginationHelper = ModelPaginationHelper<PinModel, QPinModel>()

    private fun getTagsForPin(pinId: UUID): List<Tag> =
        QPinTagModel()
            .pin.id
            .equalTo(pinId)
            .fetch("tag")
            .findList()
            .map { it.tag.toDomain() }

    override fun savePin(pin: Pin): Pin {
        val pinModel = sqlRepository.saveAndReturn(pin.toModel())
        savePinTags(pinModel, pin.tags)
        return pinModel.toDomain(getTagsForPin(pinModel.id))
    }

    private fun savePinTags(
        pinModel: PinModel,
        tags: List<Tag>,
    ) {
        // Get the new tag IDs
        val updatedTagIds = tags.map { it.id }.toSet()
        val existingTagIds =
            QPinTagModel()
                .pin.id
                .equalTo(pinModel.id)
                .findList()
                .map { it.tag.id }
                .toSet()

        // Remove the appropriate ones
        val removedTagIds = existingTagIds.minus(updatedTagIds)
        QPinTagModel()
            .pin.id
            .equalTo(pinModel.id)
            .tag.id
            .isIn(removedTagIds)
            .delete()

        // Persist the new tags
        val newTagIds = updatedTagIds.minus(existingTagIds)
        tags
            .filter { newTagIds.contains(it.id) }
            .map { tag -> PinTagModel(pin = pinModel, tag = tag.toModel()) }
            .forEach { database.save(it) }
    }

    override fun findPinById(id: UUID): Pin? {
        val pin =
            QPinModel()
                .id
                .equalTo(id)
                .findOne() ?: return null
        return pin.toDomain(getTagsForPin(pin.id))
    }

    override fun findPinByIdForUser(
        id: UUID,
        reader: User,
    ): Pin? {
        val pin =
            QPinModel()
                .id
                .equalTo(id)
                .author.id
                .equalTo(reader.id)
                .findOne() ?: return null
        return pin.toDomain(getTagsForPin(pin.id))
    }


    override fun findPinsForUserPaginated(
        user: User,
        cursor: UUID?,
        direction: PaginationDirection,
        pageSize: Int,
        sortStrategy: PinSortStrategy,
    ): Page<Pin> {
        val cursor = cursor?.let { QPinModel().id.equalTo(it).findOne() }?.let {
            Cursor(
                pivot = it,
                direction = direction,
            )
        }
        val modelPage = pinModelPaginationHelper.getPage(
            cursor = cursor,
            pageSize = pageSize,
            baseQuery = QPinModel().author.id.equalTo(user.id),
            sortStrategy = PinModelSortStrategy.fromDomain(sortStrategy)
        )
        return Page(
            items = modelPage.items.map { it.toDomain(getTagsForPin(it.id)) },
            nextCursor = modelPage.nextCursor,
            previousCursor = modelPage.previousCursor,
        )
    }
}