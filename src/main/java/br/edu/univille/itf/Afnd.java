package br.edu.univille.itf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Afnd {
    private JsonNode afndData;

    private JsonNode sequences;

    private Logger logger = Logger.getLogger(this.getClass().getName());


    private List<String> inicialStatus = new ArrayList<>();

    private List<String> finalStatus = new ArrayList<>();

    private List<String> lista = new ArrayList<>();

    private JsonNode matrix;

    private ArrayList<JsonNode> approved = new ArrayList<>();

    private ArrayList<JsonNode> rejected = new ArrayList<>();

    private Map<String, List<JsonNode>> results = new HashMap<>();


    private void getJsonInfos(){
        logger.info("Getting Info from Json");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            afndData = objectMapper.readTree(new File("src/main/resources/afnddata.json"));
            sequences = objectMapper.readTree(new File("src/main/resources/sequences.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setup(){
        getJsonInfos();
        logger.info("Getting inicial Status");
        afndData.get("inicialStatus").forEach(
                status -> {
                    inicialStatus.add(status.asText());
                    lista.add(status.asText());
                }
        );
        logger.info("Getting final Status");
        afndData.get("FinalStatus").forEach(
                status -> finalStatus.add(status.asText())
        );
        logger.info("Getting Matrix");
        matrix = afndData.get("matrix");
    }

    public void execute(){
        setup();
        for (JsonNode sequenceList: sequences.get("sequences")) {
            for (JsonNode input: sequenceList) {
                List<String> tempList = new ArrayList<>();
                lista.forEach(status -> {
                    JsonNode jsonNode = matrix.get(status).get(input.asText());
                    if(jsonNode != null){
                        jsonNode.forEach(statusFromJsonNode -> {
                            tempList.add(statusFromJsonNode.asText());
                        });
                    }
                });
                List<String> tempList2 = new ArrayList<>(tempList);
                tempList2.forEach(statusInside ->{
                    JsonNode temp3 = matrix.get(statusInside).get("ε");
                    if (temp3 != null){
                        temp3.forEach(statusInside2 -> tempList.add(statusInside2.asText()));
                    }
                });
                lista = new ArrayList<>(tempList);
            }
            if(finalStatus.stream().anyMatch(finalStatus -> lista.contains(finalStatus))){
                approved.add(sequenceList);
            } else {
                rejected.add(sequenceList);
            };
        }
        saveResults();

    }

    private void saveResults(){
        results.put("Approved", approved);
        results.put("Rejected", rejected);
        logger.info("Saving results into results.json");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File("src/main/resources/results.json"), this.results);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
