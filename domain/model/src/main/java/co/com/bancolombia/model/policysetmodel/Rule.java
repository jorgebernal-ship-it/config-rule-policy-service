package co.com.bancolombia.model.policysetmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Rule {
    private String id;
    private String policyId;
    private String name;
    private String effect;
    private String decisionCode;
    private Map<String, Object> target;
    private String whenAttribute;
    private String whenOperator;
    private String whenValue;
    private Integer sequence;
}
