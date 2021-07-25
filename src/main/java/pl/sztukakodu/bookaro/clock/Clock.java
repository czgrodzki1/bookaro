package pl.sztukakodu.bookaro.clock;

import java.time.Duration;
import java.time.LocalDateTime;

public interface Clock {

    LocalDateTime now();

    class MockClock implements Clock {
        private LocalDateTime time;

        public MockClock(LocalDateTime time) {
            this.time = time;
        }

        public MockClock() {
            this(LocalDateTime.now());
        }

        @Override
        public LocalDateTime now() {
            return time;
        }

        public void tick(Duration duration) {
            time = time.plus(duration);
        }
    }
}
