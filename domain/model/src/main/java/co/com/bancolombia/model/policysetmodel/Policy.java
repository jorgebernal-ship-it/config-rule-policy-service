package co.com.bancolombia.model.policysetmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Policy {
    private String id;
    private String policySetId;
    private String name;
    private String algorithmCombinationRules;
    private Integer sequence;
    private List<Rule> rules;
}
