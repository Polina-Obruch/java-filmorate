package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
public class Event {

    private Integer eventId;
    private Integer userId;
    private Integer entityId;
    private EventType eventType;
    private EventOperation operation;
    private Long timestamp;
}
