package com.org.ticketmaster.search.document;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Denormalized, read-optimized projection of an {@link com.org.ticketmaster.model.Event}
 * indexed into Elasticsearch. Kept in sync with Postgres via application events published
 * from EventService - see EventSearchIndexListener.
 */
@Document(indexName = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Text)
    private String venueName;

    @Field(type = FieldType.Text)
    private String location;

    @Field(type = FieldType.Text)
    private String performerName;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime eventDate;

    @Field(type = FieldType.Long)
    private Long venueId;

    @Field(type = FieldType.Long)
    private Long performerId;
}