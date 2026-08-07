package com.ktb.lukas.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(indexName = "posts")
@Setting(settingPath = "elasticsearch/nori-settings.json")
@Builder
public class PostDocument{

    @Id
    @Field(type = FieldType.Keyword)
    private Long id;

    @Field(type = FieldType.Text, analyzer = "search_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "search_analyzer")
    private String content;

    public void updateFrom(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
    }


}
