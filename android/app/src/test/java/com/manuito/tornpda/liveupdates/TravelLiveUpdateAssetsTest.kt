package com.manuito.tornpda.liveupdates

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TravelLiveUpdateAssetsTest {

    @Test
    fun endpointIconForKnownPlacesReturnsDistinctDrawables() {
        val places = listOf(
            "Argentina", "Canada", "Cayman Islands", "China", "Hawaii",
            "Japan", "Mexico", "South Africa", "Switzerland", "UAE", "United Kingdom",
            "Torn", "Hospital",
        )
        val icons = places.map { TravelLiveUpdateAssets.endpointIconFor(it) }.toSet()
        assertEquals("each known place maps to a unique drawable", places.size, icons.size)
    }

    @Test
    fun endpointIconForHandlesCaseAndWhitespace() {
        assertEquals(
            TravelLiveUpdateAssets.endpointIconFor("Mexico"),
            TravelLiveUpdateAssets.endpointIconFor("  mexico  "),
        )
    }

    @Test
    fun endpointIconForUnknownFallsBackToGlobe() {
        val unknown = TravelLiveUpdateAssets.endpointIconFor("Atlantis")
        assertEquals(unknown, TravelLiveUpdateAssets.endpointIconFor("Abroad"))
        assertNotEquals(TravelLiveUpdateAssets.endpointIconFor("Mexico"), unknown)
    }

    @Test
    fun endpointIconNeverReusesTheTrackerPlane() {
        val tracker = TravelLiveUpdateAssets.trackerIcon()
        listOf("Torn", "Mexico", "Hospital", "Atlantis").forEach {
            assertNotEquals("$it must not render as the plane", tracker, TravelLiveUpdateAssets.endpointIconFor(it))
        }
    }

    @Test
    fun trackerIconIsDirectionAgnostic() {
        // The bar always fills left-to-right, so the plane must not mirror per leg
        assertEquals(TravelLiveUpdateAssets.trackerIcon(), TravelLiveUpdateAssets.trackerIcon())
        assertEquals(TravelLiveUpdateAssets.smallIconFor("Mexico"), TravelLiveUpdateAssets.trackerIcon())
    }

    @Test
    fun smallIconFlipsForHomewardTrip() {
        assertNotEquals(
            TravelLiveUpdateAssets.smallIconFor("Mexico"),
            TravelLiveUpdateAssets.smallIconFor("Torn"),
        )
        assertEquals(
            TravelLiveUpdateAssets.smallIconFor("Torn"),
            TravelLiveUpdateAssets.smallIconFor("  torn  "),
        )
    }
}
