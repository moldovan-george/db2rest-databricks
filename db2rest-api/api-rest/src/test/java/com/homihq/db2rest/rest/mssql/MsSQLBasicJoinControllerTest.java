package com.homihq.db2rest.rest.mssql;


import io.hosuaby.inject.resources.junit.jupiter.GivenJsonResource;
import io.hosuaby.inject.resources.junit.jupiter.TestWithResources;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Order(501)
@TestWithResources
class MsSQLBasicJoinControllerTest extends MsSQLBaseIntegrationTest {

    @GivenJsonResource(TEST_JSON_FOLDER + "/LEFT_JOIN.json")
    List<Map<String, Object>> LEFT_JOIN;

    @GivenJsonResource(TEST_JSON_FOLDER + "/RIGHT_JOIN.json")
    List<Map<String, Object>> RIGHT_JOIN;

    @Test
    @DisplayName("Left Join")
    void leftJoin() throws Exception {
        mockMvc.perform(post(getPrefixApiUrl() + "/users/_expand")
                        .contentType(APPLICATION_JSON).accept(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LEFT_JOIN))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.*").isArray())
                .andExpect(jsonPath("$.data.*", hasSize(4)))

                .andExpect(jsonPath("$.data[0].*", hasSize(10)))
                .andExpect(jsonPath("$.data[0].auid", equalTo(1)))
                .andExpect(jsonPath("$.data[0].apid", equalTo(1)))

                .andExpect(jsonPath("$.data[1].auid", equalTo(2)))
                .andExpect(jsonPath("$.data[1].apid", nullValue()))

                .andExpect(jsonPath("$.data[3].auid", equalTo(6)))
                .andExpect(jsonPath("$.data[3].apid", nullValue()))
                .andExpect(jsonPath("$.data[3].firstname", nullValue()))

                .andDo(document(DB_NAME + "-left-join"));
    }

    @Test
    @DisplayName("Right Join")
    void rightJoin() throws Exception {
        mockMvc.perform(post(getPrefixApiUrl() + "/users/_expand")
                        .contentType(APPLICATION_JSON).accept(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(RIGHT_JOIN))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.*").isArray())
                .andExpect(jsonPath("$.data.*", hasSize(4)))
                .andExpect(jsonPath("$.data[0].*", hasSize(10)))
                .andExpect(jsonPath("$.data[0].auid", equalTo(1)))
                .andExpect(jsonPath("$.data[0].apid", equalTo(1)))
                .andExpect(jsonPath("$.data[0].username", equalTo("admin")))
                .andExpect(jsonPath("$.data[0].firstname", equalTo("Jack")))

                .andExpect(jsonPath("$.data[1].apid", equalTo(2)))
                .andExpect(jsonPath("$.data[1].auid", equalTo(3)))
                .andExpect(jsonPath("$.data[1].username", nullValue()))
                .andExpect(jsonPath("$.data[1].firstname", equalTo("Tom")))

                .andExpect(jsonPath("$.data[2].auid", equalTo(5)))
                .andExpect(jsonPath("$.data[2].apid", equalTo(4)))
                .andExpect(jsonPath("$.data[2].username", nullValue()))
                .andExpect(jsonPath("$.data[2].firstname", equalTo("Bill")))

                .andDo(document(DB_NAME + "-right-join"));
    }

}
