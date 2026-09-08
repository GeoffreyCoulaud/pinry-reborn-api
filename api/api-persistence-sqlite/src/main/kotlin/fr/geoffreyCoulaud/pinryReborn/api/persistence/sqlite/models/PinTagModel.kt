package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models

import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne

/**
 * Many-to-many join between tags and pins.
 * This is done to avoid interdependency of models and repos.
 */
@Entity
class PinTagModel(
    @ManyToOne var pin: PinModel,
    @ManyToOne var tag: TagModel,
)
