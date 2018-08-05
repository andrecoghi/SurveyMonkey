package de.marketlogicsoftware.task.surveyservice.controller;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import de.marketlogicsoftware.task.surveyservice.controller.handler.NotFoundException;
import de.marketlogicsoftware.task.surveyservice.controller.handler.BadRequestException;
import de.marketlogicsoftware.task.surveyservice.dto.SurveyResultDTO;
import de.marketlogicsoftware.task.surveyservice.persistence.entity.SurveyEntity;
import de.marketlogicsoftware.task.surveyservice.persistence.service.SurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RequestMapping("survey")
@RestController
public class SurveyController {

    @Autowired
    private SurveyService surveyService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EurekaClient eurekaClient;

    @Value("${service.question.serviceId}")
    private String questionServiceId;


    @PostMapping
    public ResponseEntity create(@RequestBody SurveyEntity survey) {
        validateRequest(survey);
        surveyService.save(survey);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/question/{questionId}/statistics")
    public ResponseEntity<SurveyResultDTO> getQuestionResult(@PathVariable Long questionId) {
        SurveyResultDTO surveyResult = surveyService.getStatistics(questionId);
        if (surveyResult.getTotalNumber() == 0) {
            throw new BadRequestException("No survey has been done for this question");
        }
        return ResponseEntity.ok(surveyResult);
    }

    private void validateRequest(SurveyEntity survey) {
        if (survey.getQuestionId() == null) {
            throw new BadRequestException("Question id is a must");
        }
        if (survey.getAnswerId() == null) {
            throw new BadRequestException("Answer id is a must");
        }

        StringBuilder validationUrl = new StringBuilder("http://");

        Application application = eurekaClient.getApplication(questionServiceId);
        if (application != null) {
            InstanceInfo instanceInfo = application.getInstances().get(0);
            validationUrl.append(instanceInfo.getIPAddr()).append(":")
                    .append(instanceInfo.getPort()).append("/answers/search/exists");
        }

        boolean exists = restTemplate.getForObject(validationUrl.toString(), Boolean.class);

        if (!exists) {
            throw new NotFoundException("The question and answer combination is not found");
        }
    }

}
