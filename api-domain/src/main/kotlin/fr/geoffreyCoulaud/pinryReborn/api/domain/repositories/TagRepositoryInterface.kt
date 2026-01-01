package fr.geoffreyCoulaud.pinryReborn.api.domain.repositories

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Tag
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User

interface TagRepositoryInterface {
    fun saveTag(tag: Tag): Tag

    fun findTagByName(name: String): Tag?

    fun findAllUserTags(user: User): List<Tag>
}
