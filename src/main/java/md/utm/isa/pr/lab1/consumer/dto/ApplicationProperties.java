package md.utm.isa.pr.lab1.consumer.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@ConfigurationProperties(prefix = "")
public class ApplicationProperties {
    private List<CookDto> cooksList;

    public List<CookDto> getCooksList() {
        return cooksList;
    }

    public void setCooksList(List<CookDto> cooksList) {
        this.cooksList = cooksList;
    }

    public static class CookProps {
        private String rank;
        private String proficiency;
        private String name;

        public String getRank() {
            return rank;
        }

        public void setRank(String rank) {
            this.rank = rank;
        }

        public String getProficiency() {
            return proficiency;
        }

        public void setProficiency(String proficiency) {
            this.proficiency = proficiency;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
