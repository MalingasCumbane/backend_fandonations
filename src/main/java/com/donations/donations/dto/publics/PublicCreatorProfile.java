package com.donations.donations.dto.publics;

import com.donations.donations.model.Campaign;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class PublicCreatorProfile {
    private PublicCreator creator;
    private List<Campaign> campaigns;
    private Map<String, Integer> stats;
}
