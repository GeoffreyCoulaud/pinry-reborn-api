package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.repositories

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Page
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.PaginationDirection
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.PaginationDirection.BACKWARD
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.PaginationDirection.FORWARD
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.PinSortStrategy
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.PinSortStrategy.CREATED_AT_ASC
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.PinSortStrategy.CREATED_AT_DESC
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Tag
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.PinRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.PinModelMapper.toDomain
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.PinModelMapper.toModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.TagModelMapper.toDomain
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.TagModelMapper.toModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.PinModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.PinTagModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.query.QPinModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.query.QPinTagModel
import io.ebean.Database
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class PinRepository(
    private val database: Database,
) : PinRepositoryInterface {
    private val sqlRepository = ModelRepository(entityClass = PinModel::class, database = database)

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

    override fun findAllPinsForUser(user: User): List<Pin> =
        QPinModel()
            .author.id
            .equalTo(user.id)
            .findList()
            .map { it.toDomain(getTagsForPin(it.id)) }

    override fun findPinsForUserPaginated(
        user: User,
        cursor: UUID?,
        direction: PaginationDirection,
        pageSize: Int,
        sort: PinSortStrategy,
    ): Page<Pin> {

        // Build the base query for the user's pins
        var query = QPinModel().author.id.equalTo(user.id)

        // Apply cursor-based filtering based on strategy
        val cursorPin = cursor?.let { QPinModel().id.equalTo(it).findOne() }
        if (cursorPin != null) {
            query = when (sort) {
                CREATED_AT_ASC -> when (direction) {
                    FORWARD -> query.whenCreated.greaterThan(cursorPin.whenCreated)
                    BACKWARD -> query.whenCreated.lessThan(cursorPin.whenCreated)
                }

                CREATED_AT_DESC -> when (direction) {
                    FORWARD -> query.whenCreated.lessThan(cursorPin.whenCreated)
                    BACKWARD -> query.whenCreated.greaterThan(cursorPin.whenCreated)
                }
            }
        }

        // Apply sorting
        query = when (sort) {
            CREATED_AT_ASC -> when (direction) {
                FORWARD -> query.orderBy().whenCreated.asc()
                BACKWARD -> query.orderBy().whenCreated.desc()
            }

            CREATED_AT_DESC -> when (direction) {
                FORWARD -> query.orderBy().whenCreated.desc()
                BACKWARD -> query.orderBy().whenCreated.asc()
            }
        }

        // Fetch one extra to check if there are more results
        val hasMoreInDirection: Boolean
        val pins = query.setMaxRows(pageSize + 1)
            .findList()
            .apply {
                // Determine if there are more pages
                hasMoreInDirection = size > pageSize
                if (hasMoreInDirection) dropLast(1)
            }
            .apply {
                // For backward direction, reverse the results to maintain consistent order
                if (direction == BACKWARD) reverse()
            }

        // Determine cursors for next/previous pages
        val nextCursor: UUID? = when (direction) {
            FORWARD -> if (hasMoreInDirection) pins.lastOrNull()?.id else null
            BACKWARD -> pins.lastOrNull()?.id
        }
        val previousCursor: UUID? = when (direction) {
            FORWARD -> pins.firstOrNull()?.id
            BACKWARD -> if (hasMoreInDirection) pins.firstOrNull()?.id else null

        }

        // Convert to domain objects
        return Page(
            items = pins.map { it.toDomain(getTagsForPin(it.id)) },
            nextCursor = nextCursor,
            previousCursor = previousCursor,
        )
    }
}
