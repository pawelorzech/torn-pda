package com.manuito.tornpda.liveupdates

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TravelLiveUpdateAssetsTest {

    @Test
    fun endpointIconForKnownDestinationsReturnsDistinctDrawables() {
        val destinations = listOf(
            "Argentina", "Canada", "Cayman Islands", "China", "Hawaii",
            "Japan", "Mexico", "South Africa", "Switzerland", "UAE", "United Kingdom", "Torn",
        )
        val icons = destinations.map { TravelLiveUpdateAssets.endpointIconFor(it) }.toSet()
        assertEquals("each known destination maps to a unique drawable", destinations.size, icons.size)
    }

    @Test
    fun endpointIconForHandlesCaseAndWhitespace() {
        assertEquals(
            TravelLiveUpdateAssets.endpointIconFor("Mexico"),
            TravelLiveUpdateAssets.endpointIconFor("  mexico  "),
        )
    }

    @Test
    fun endpointIconForUnknownFallsBackToDefault() {
        val unknown = TravelLiveUpdateAssets.endpointIconFor("Atlantis")
        val mexico = TravelLiveUpdateAssets.endpointIconFor("Mexico")
        assertNotEquals(mexico, unknown)
        assertNotEquals("unknown endpoints must not render as a plane", TravelLiveUpdateAssets.trackerIconFor(), unknown)
    }

    @Test
    fun contextualEndpointsDoNotRenderAsTrackerPlane() {
        assertNotEquals(
            TravelLiveUpdateAssets.trackerIconFor(),
            TravelLiveUpdateAssets.endpointIconFor("Abroad"),
        )
        assertNotEquals(
            TravelLiveUpdateAssets.trackerIconFor(),
            TravelLiveUpdateAssets.endpointIconFor("Hospital"),
        )
        assertNotEquals(
            TravelLiveUpdateAssets.trackerIconFor(),
            TravelLiveUpdateAssets.endpointIconFor("Torn"),
        )
    }

    @Test
    fun notificationIconDoesNotRenderAsTrackerPlane() {
        assertNotEquals(
            TravelLiveUpdateAssets.trackerIconFor(),
            TravelLiveUpdateAssets.notificationIcon(),
        )
    }
}
