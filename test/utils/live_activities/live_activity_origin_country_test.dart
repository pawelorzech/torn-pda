import 'package:flutter_test/flutter_test.dart';
import 'package:torn_pda/utils/live_activities/live_activity_travel_controller.dart';

void main() {
  group('parseCountryFromStatusDescription', () {
    test('recovers the origin country from a returning status', () {
      expect(
        LiveActivityTravelController.parseCountryFromStatusDescription("Returning to Torn from Cayman Islands"),
        "Cayman Islands",
      );
    });

    test('recovers the destination country from an outbound status', () {
      expect(
        LiveActivityTravelController.parseCountryFromStatusDescription("Traveling to South Africa"),
        "South Africa",
      );
    });

    test('prefers the longest match so multi-word countries win', () {
      expect(
        LiveActivityTravelController.parseCountryFromStatusDescription("Returning to Torn from United Arab Emirates"),
        "United Arab Emirates",
      );
    });

    test('is case insensitive', () {
      expect(
        LiveActivityTravelController.parseCountryFromStatusDescription("returning to torn from japan"),
        "Japan",
      );
    });

    test('returns null when no known country appears', () {
      expect(LiveActivityTravelController.parseCountryFromStatusDescription("In hospital"), isNull);
      expect(LiveActivityTravelController.parseCountryFromStatusDescription("Returning to Torn"), isNull);
      expect(LiveActivityTravelController.parseCountryFromStatusDescription(""), isNull);
      expect(LiveActivityTravelController.parseCountryFromStatusDescription(null), isNull);
    });
  });
}
