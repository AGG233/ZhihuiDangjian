package com.rauio.smartdangjian.server.graph.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.graph.pojo.response.PartySeedImportResponse;
import com.rauio.smartdangjian.server.graph.service.PartySeedDataImporter;

@ExtendWith(MockitoExtension.class)
class PartyGraphAdminControllerTest {

    @Mock
    private PartySeedDataImporter partySeedDataImporter;

    @InjectMocks
    private PartyGraphAdminController controller;

    @Test
    @DisplayName("importSeed 委托导入器并返回导入计数")
    void importSeed() {
        PartySeedImportResponse response = PartySeedImportResponse.builder()
                .personCount(10)
                .eventCount(8)
                .theoryCount(6)
                .personEventCount(16)
                .documentTheoryCount(8)
                .total(48)
                .build();
        when(partySeedDataImporter.importAll()).thenReturn(response);

        var result = controller.importSeed();

        assertThat(result).isNotNull();
        assertThat(result.getData()).isEqualTo(response);
        assertThat(result.getData().getPersonCount()).isEqualTo(10);
        assertThat(result.getData().getTotal()).isEqualTo(48);
    }
}
