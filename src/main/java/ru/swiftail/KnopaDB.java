package ru.swiftail;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.swiftail.dto.PollDto;

import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
@RequiredArgsConstructor
public class KnopaDB {
    private static final Path dataDir = Path.of("data").toAbsolutePath();
    private static final Map<UUID, PollDto> data = new HashMap<>();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor();

    public PollDto getPoll(UUID id) {
        synchronized (this) {
            return data.get(id);
        }
    }

    public void updatePoll(PollDto newPoll) {
        synchronized (this) {
            data.put(newPoll.id(), newPoll);
        }
        IO_EXECUTOR.submit(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {
                var path = dataDir.resolve("%s.json".formatted(newPoll.id()));
                var data = OBJECT_MAPPER.writeValueAsBytes(newPoll);
                Files.write(path, data);
            }
        });
    }

    @SneakyThrows
    public void load() {
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
        }
        try (var stream = Files.walk(dataDir)) {
            var files = stream.filter(file -> file.getFileName().toString().endsWith(".json")).toList();

            for (var file : files) {
                var fileData = Files.readAllBytes(file);
                var poll = OBJECT_MAPPER.readValue(fileData, PollDto.class);
                data.put(poll.id(), poll);
            }
        }
    }
}
