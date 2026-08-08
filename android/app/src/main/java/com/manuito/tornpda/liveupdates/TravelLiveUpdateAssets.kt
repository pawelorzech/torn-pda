package com.manuito.tornpda.liveupdates

import com.manuito.tornpda.R

object TravelLiveUpdateAssets {

    /**
     * Icon for either end of the ProgressStyle bar. The bar always reads
     * left-to-right as origin -> destination, so both ends use the same
     * round "ball" set shared with the iOS Live Activity.
     */
    fun endpointIconFor(displayName: String?): Int {
        return when (displayName?.trim()?.lowercase()) {
            "torn" -> R.drawable.ball_torn
            "argentina" -> R.drawable.ball_argentina
            "canada" -> R.drawable.ball_canada
            "cayman islands", "cayman", "cayman island" -> R.drawable.ball_cayman
            "china" -> R.drawable.ball_china
            "hawaii" -> R.drawable.ball_hawaii
            "japan" -> R.drawable.ball_japan
            "mexico" -> R.drawable.ball_mexico
            "south africa" -> R.drawable.ball_south_africa
            "switzerland" -> R.drawable.ball_switzerland
            "uae", "united arab emirates" -> R.drawable.ball_uae
            "united kingdom", "uk" -> R.drawable.ball_uk
            "hospital" -> R.drawable.hospital
            else -> R.drawable.ball_world
        }
    }

    /**
     * Icon riding the ProgressStyle bar. The bar fills left-to-right regardless
     * of travel direction, so the plane always faces right; a mirrored plane
     * would read as flying backwards against its own motion.
     */
    fun trackerIcon(): Int = R.drawable.plane_right

    /**
     * Status-bar / chip icon. There is no bar to give it context here, so this
     * is the one place direction carries meaning: outbound vs heading home.
     */
    fun smallIconFor(destinationDisplayName: String?): Int {
        return when (destinationDisplayName?.trim()?.lowercase()) {
            "torn" -> R.drawable.plane_left
            else -> R.drawable.plane_right
        }
    }
}
