import 'package:flutter_test/flutter_test.dart';
import 'package:tulungin/models.dart';

void main() {
  group('JobStatusText.parse', () {
    test('accepts Firestore status variants', () {
      expect(JobStatusText.parse('AVAILABLE'), JobStatus.available);
      expect(JobStatusText.parse('IN_PROGRESS'), JobStatus.inProgress);
      expect(
        JobStatusText.parse('awaiting_confirmation'),
        JobStatus.awaitingConfirmation,
      );
      expect(JobStatusText.parse('COMPLETED'), JobStatus.completed);
    });

    test('unknown and absent statuses default to available', () {
      expect(JobStatusText.parse(null), JobStatus.available);
      expect(JobStatusText.parse('unknown'), JobStatus.available);
    });
  });
}
