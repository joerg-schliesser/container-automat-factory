/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.containerautomat.processing.runtime;

import de.containerautomat.config.ContainerAutomatCoreConfig;
import de.containerautomat.processing.ContainerAutomatCommand;
import de.containerautomat.processing.ContainerAutomatCommandProcessor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.function.Function;

/**
 * A Spring bean that provides functionality to simulate application-specific
 * processing logic that takes some time and whose duration depends on
 * chance within certain limits.
 * <p/>
 * Implementations of {@link ContainerAutomatCommandProcessor#processCommand(ContainerAutomatCommand)}
 * pass a reference to the {@link #simulateWork(ContainerAutomatCommand)} method of this class
 * to the {@link ContainerAutomatRuntimeProcessor#processCommand(ContainerAutomatCommand, Function)}
 * method.
 */
@Component
@ConditionalOnProperty(value = ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_STATE)
@Getter
public class ContainerAutomatWorkSimulator {

    static final String WORKMESSAGE_WITH_INPUT_SYMBOL_TEMPLATE = "Simulated work for input symbol %s with a random duration of %s milliseconds.";

    static final String WORKMESSAGE_WITHOUT_INPUT_SYMBOL_TEMPLATE = "Simulated work with a random duration of %s milliseconds.";

    private static final Random random = new Random();


    private final long minDurationMillis;

    private final long maxDurationMillis;


    public ContainerAutomatWorkSimulator(@Value("${" + ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_PROCESSING_MIN_DURATION_MILLIS + ":500}") long minDurationMillis, @Value("${" + ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_PROCESSING_MAX_DURATION_MILLIS + ":5000}") long maxDurationMillis) {

        if (minDurationMillis < 0 || maxDurationMillis < minDurationMillis) {
            throw new IllegalArgumentException("Invalid Configuration for ContainerAutomatWorkSimulator because of a minimum of %s and a maximum of %s milliseconds.".formatted(minDurationMillis, maxDurationMillis));
        }
        this.minDurationMillis = minDurationMillis;
        this.maxDurationMillis = maxDurationMillis;
    }

    public ContainerAutomatRuntimeProcessor.ContainerAutomatWorkResult simulateWork(ContainerAutomatCommand containerAutomatCommand) {

        var startMillis = System.currentTimeMillis();
        var randomDuration = getRandomDuration();
        if (randomDuration > 0) {
            try {
                Thread.sleep(randomDuration);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        var durationMillis = System.currentTimeMillis() - startMillis;
        if (containerAutomatCommand.hasInputSymbol()) {
            return new ContainerAutomatRuntimeProcessor.ContainerAutomatWorkResult(WORKMESSAGE_WITH_INPUT_SYMBOL_TEMPLATE.formatted(containerAutomatCommand.currentInputSymbol().orElseThrow(), durationMillis), durationMillis);
        }
        return new ContainerAutomatRuntimeProcessor.ContainerAutomatWorkResult(WORKMESSAGE_WITHOUT_INPUT_SYMBOL_TEMPLATE.formatted(durationMillis), durationMillis);
    }

    protected long getRandomDuration() {

        return minDurationMillis + ((maxDurationMillis == minDurationMillis) ? 0 : random.nextLong(maxDurationMillis - minDurationMillis + 1));
    }

}
