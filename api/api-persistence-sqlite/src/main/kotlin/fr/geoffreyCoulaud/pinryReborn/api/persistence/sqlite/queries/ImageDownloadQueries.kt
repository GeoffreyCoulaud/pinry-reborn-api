package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.queries

import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.query.QImageDownloadModel

// A download row is not itself recyclable, so it has no state to ask about until its pin is
// navigated: an extension, like the pin-to-board join's, is how that reaches the queries package.

/** Downloads whose pin is not in the recycle bin. */
fun QImageDownloadModel.withActivePin(): QImageDownloadModel = pin.softDeletedAt.isNull
