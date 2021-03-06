package codecamp.bug.wars.ai.controller;

import codecamp.bug.wars.ai.exceptions.AIScriptNotFoundException;
import codecamp.bug.wars.ai.exceptions.InvalidInputException;
import codecamp.bug.wars.ai.exceptions.NameUnavailableException;
import codecamp.bug.wars.ai.exceptions.ParseErrorException;
import codecamp.bug.wars.ai.model.AIScript;
import codecamp.bug.wars.ai.model.AIScriptResponse;
import codecamp.bug.wars.ai.model.CompiledScriptResponse;
import codecamp.bug.wars.ai.service.AIService;
import codecamp.bug.wars.ai.service.compiler.Compiler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;

// Unit Test
// We ISOLATE the class that we're testing.
// Which means we test the REAL VERSION of AIController
// BUT EVERYTHING AROUND IT IS FAKE
public class AIControllerTest {
    private AIService mockAIService;
    private AIController aiController;
    private Compiler mockCompiler;
    @BeforeEach
    public void setup(){
        mockAIService = Mockito.mock(AIService.class);
        mockCompiler = Mockito.mock(Compiler.class);
        aiController = new AIController(mockAIService, mockCompiler);
    }

    //Service will have 3 different responses
    // 1. It was Successful at saving and returns saved AIScript
    // 2. It was Not Successful and throws an InvalidInputException. Error 400
    // 3. It was Not Successful and throws a NameUnavailableException. Error 409

    @Test
    public void createAIScript_ShouldReturnAIScriptWithIdAndOKHttpStatus(){
        // arrange
        AIScript postBodyInput = new AIScript(null, "Meg", "jump jump");
        AIScript savedScript = new AIScript(1L, "Meg", "jump jump");
        AIScriptResponse expected = new AIScriptResponse(savedScript, null);
        Mockito.when(mockAIService.saveAI(Mockito.any())).thenReturn(savedScript);

        // act
        ResponseEntity<AIScriptResponse> response = aiController.createAIScript(postBodyInput);

        // assert
        assertEquals(expected, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void createAIScript_ShouldReturn400IfAIScriptRejectedByService(){
        // arrange
        AIScript postBodyInput = new AIScript(null, null, "jump jump");
        Mockito.when(mockAIService.saveAI(Mockito.any())).thenThrow(new InvalidInputException("Name cannot be null"));

        // act
        ResponseEntity<AIScriptResponse> responseEntity = aiController.createAIScript(postBodyInput);
        AIScriptResponse response = responseEntity.getBody();

        // assert
        assertEquals(postBodyInput, response.getAi());
        assertEquals("Name cannot be null", response.getError());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void createAIScript_ShouldReturn409IfServiceThrowsNameNotAvailable(){
        // arrange
        AIScript postBodyInput = new AIScript(null, "John", "jump jump");
        Mockito.when(mockAIService.saveAI(Mockito.any())).thenThrow(new NameUnavailableException("Name not available"));

        // act
        ResponseEntity<AIScriptResponse> responseEntity = aiController.createAIScript(postBodyInput);
        AIScriptResponse response = responseEntity.getBody();

        // assert
        assertEquals(postBodyInput, response.getAi());
        assertEquals("Name not available", response.getError());
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
    }

    // 2 responses
    // 1. empty list
    // 2. list with scripts

    @Test
    public void getAllAI_ShouldReturnEmptyListIfEmpty(){
        // arrange
        List<AIScript> expected = new ArrayList<>();
        Mockito.when(mockAIService.getAllAI()).thenReturn(expected);

        // act
        ResponseEntity<List<AIScript>> response = aiController.getAllAI();

        // assert
        assertEquals(expected, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getAllAI_ShouldReturnListOfAIScripts(){
        // arrange
        List<AIScript> expected = new ArrayList<>();
        expected.add(new AIScript(1L, "Meg", "jump jump"));
        expected.add(new AIScript(2L, "Kellsey", "move"));
        Mockito.when(mockAIService.getAllAI()).thenReturn(expected);

        // act
        ResponseEntity<List<AIScript>> response = aiController.getAllAI();

        // assert
        assertEquals(expected, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    public void updateAIScript_ShouldCallService_ReturnUpdatedRecord() {
        AIScript postBodyInput = new AIScript(1L, "Some Name", "jump jump");
        Mockito.when(mockAIService.updateAI(postBodyInput, 1L)).thenReturn(postBodyInput);

        ResponseEntity<AIScriptResponse> response = aiController.updateAIScript(postBodyInput, 1L);

        verify(mockAIService).updateAI(postBodyInput, 1L);
        assertEquals(postBodyInput, response.getBody().getAi());
        assertEquals(null, response.getBody().getError());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void updateAIScript_ShouldReturn400IfAIScriptRejectedByService(){
        // arrange
        AIScript postBodyInput = new AIScript(null, null, "jump jump");
        Mockito.when(mockAIService.updateAI(postBodyInput, 1L)).thenThrow(new InvalidInputException("Name cannot be null"));

        // act
        ResponseEntity<AIScriptResponse> responseEntity = aiController.updateAIScript(postBodyInput, 1L);
        AIScriptResponse response = responseEntity.getBody();

        // assert
        assertEquals(postBodyInput, response.getAi());
        assertEquals("Name cannot be null", response.getError());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void updateAIScript_ShouldReturn409IfServiceThrowsNameNotAvailable(){
        // arrange
        AIScript postBodyInput = new AIScript(null, "John", "jump jump");
        Mockito.when(mockAIService.updateAI(postBodyInput, 1L)).thenThrow(new NameUnavailableException("Name not available"));

        // act
        ResponseEntity<AIScriptResponse> responseEntity = aiController.updateAIScript(postBodyInput, 1L);
        AIScriptResponse response = responseEntity.getBody();

        // assert
        assertEquals(postBodyInput, response.getAi());
        assertEquals("Name not available", response.getError());
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
    }

    @Test
    public void getAiById_ShouldReturn404IfServiceThrowsIdDoesNotExist(){
        // arrange

        Mockito.when(mockAIService.getAIById(1L)).thenThrow(new AIScriptNotFoundException("An AIScript with that ID does not exist"));

        // act
        ResponseEntity<AIScriptResponse> responseEntity = aiController.getAIById(1L);
//        AIScriptResponse response = responseEntity.getBody();

        // assert
        assertEquals("An AIScript with that ID does not exist", responseEntity.getBody().getError());
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void getAiById_ShouldReturnAIWithCorrespondingId(){
        // arrange
        AIScript existing = new AIScript(1L, "John", "turnleft move jump");
        Mockito.when(mockAIService.getAIById(1L)).thenReturn(existing);
        AIScriptResponse expectedScriptResponse = new AIScriptResponse(existing, null);
        ResponseEntity<AIScriptResponse> expected = new ResponseEntity<AIScriptResponse>(expectedScriptResponse, HttpStatus.OK);

        // act
        ResponseEntity<AIScriptResponse> responseEntity = aiController.getAIById(1L);
//        AIScriptResponse response = responseEntity.getBody();

        // assert
        assertEquals(expected, responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void compileAIScript_ShouldReturnCompiledScript(){
        // arrange
        List<Integer> bytecode = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        ResponseEntity<CompiledScriptResponse> expected = new ResponseEntity(new CompiledScriptResponse(1L, bytecode, null), HttpStatus.OK);
        Mockito.when(mockCompiler.compile(1L)).thenReturn(bytecode);

        // act
        ResponseEntity<CompiledScriptResponse> responseEntity = aiController.compileAIScript(1L);

        // assert
        assertEquals(expected, responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void compileAIScript_ShouldReturn404IfServiceThrowsAIScriptNotFound(){
        // arrange
        CompiledScriptResponse compiled = new CompiledScriptResponse(1L, null, "An AIScript with that ID does not exist");
        ResponseEntity<CompiledScriptResponse> expected = new ResponseEntity(compiled, HttpStatus.NOT_FOUND);
        Mockito.when(mockCompiler.compile(1L)).thenThrow(new AIScriptNotFoundException("An AIScript with that ID does not exist"));

        // act
        ResponseEntity<CompiledScriptResponse> responseEntity = aiController.compileAIScript(1L);

        // assert
        assertEquals(compiled, responseEntity.getBody());
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void compileAIScript_ShouldReturn422IfServiceThrowsParseErrorException(){
        // arrange
        CompiledScriptResponse compiled = new CompiledScriptResponse(1L, null, "Error parsing script");
        ResponseEntity<CompiledScriptResponse> expected = new ResponseEntity(compiled, HttpStatus.UNPROCESSABLE_ENTITY);
        Mockito.when(mockCompiler.compile(1L)).thenThrow(new ParseErrorException("Error parsing script"));

        // act
        ResponseEntity<CompiledScriptResponse> responseEntity = aiController.compileAIScript(1L);

        // assert
        assertEquals(compiled, responseEntity.getBody());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());
    }



    //save success
    //"An AI Script with that name already exists."
    //
    //"No AI Script name was assigned."
    //
    //"AI Script is required, cannot be empty."
}
