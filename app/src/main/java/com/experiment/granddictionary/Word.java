package com.experiment.granddictionary;

public class Word {
    private Long id;
    private String word;
    private String explanation;
    private Integer level;
    private Long modified_time;
    public Word() {}
    public Word(String word, String explanation, Integer level, Long modified_time) {
        this.word = word;
        this.explanation = explanation;
        this.level = level;
        this.modified_time = modified_time;
    }
    public Long getId() { return id; }
    public String getWord() { return this.word; }
    public void setWord(String word) { this.word = word; }
    public String getExplanation() { return this.explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public Integer getLevel() { return this.level; }
    public void setLevel(Integer level) { this.level = level; }
    public Long getModifiedTime() { return this.modified_time; }
    public void setModifiedTime(Long modified_time) { this.modified_time = modified_time; }
    public void put(String field, Object data) {
        if (field.equals("word")) {
            setWord((String)data);
        } else if (field.equals("explanation")) {
            setExplanation((String)data);
        } else if (field.equals("level")) {
            setLevel(Integer.valueOf((String)data));
        } else if (field.equals("modified_time")) {
            setModifiedTime(Long.valueOf((String)data));
        } else if (field.equals("id")) {
            this.id = Long.valueOf((String)data);
        }
    }
}
