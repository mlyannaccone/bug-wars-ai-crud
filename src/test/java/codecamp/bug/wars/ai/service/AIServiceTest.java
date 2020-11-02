package codecamp.bug.wars.ai.service;

import codecamp.bug.wars.ai.exceptions.InvalidInputException;
import codecamp.bug.wars.ai.exceptions.NameUnavailableException;
import codecamp.bug.wars.ai.model.AIScript;
import codecamp.bug.wars.ai.repository.AiScriptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class AIServiceTest {
    AiScriptRepository mockRepository;
    AIService service;

    @BeforeEach
    public void setup() {
        mockRepository = Mockito.mock(AiScriptRepository.class);
        service = new AIService(mockRepository);
    }

    @Test
    public void saveAI_nullInputShouldThrowError() {
        // assert
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            // act
            service.saveAI(null);
        });
        // assert
        assertEquals("AI Script is required, cannot be empty.", exception.getMessage());
    }

    @Test
    public void saveAI_nullScriptShouldThrowError() {
        // assert
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            // act
            service.saveAI(new AIScript(null, "Name", null));
        });
        // assert
        assertEquals("AI Script is required, cannot be empty.", exception.getMessage());
    }

    @Test
    public void saveAI_emptyScriptShouldThrowError() {
        // assert
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            // act
            service.saveAI(new AIScript(null, "Name", ""));
        });
        // assert
        assertEquals("AI Script is required, cannot be empty.", exception.getMessage());
    }

    @Test
    public void saveAI_whitespaceScriptShouldThrowError() {
        // assert
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            // act
            service.saveAI(new AIScript(null, "Name", "    "));
        });
        // assert
        assertEquals("AI Script is required, cannot be empty.", exception.getMessage());
    }

    @Test
    public void saveAI_nullNameShouldThrowError() {
        // assert
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            // act
            service.saveAI(new AIScript(null, null, "jump jump"));
        });
        // assert
        assertEquals("No AI Script name was assigned.", exception.getMessage());
    }

    @Test
    public void saveAI_emptyNameShouldThrowError() {
        // assert
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            // act
            service.saveAI(new AIScript(null, "", "jump jump"));
        });
        // assert
        assertEquals("No AI Script name was assigned.", exception.getMessage());
    }

    @Test
    public void saveAI_whitespaceNameShouldThrowError() {
        // assert
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            // act
            service.saveAI(new AIScript(null, "     ", "jump jump"));
        });
        // assert
        assertEquals("No AI Script name was assigned.", exception.getMessage());
    }

    @Test
    public void saveAI_NameAlreadyExists_ShouldThrowError() {
        // arrange
        AIScript inDb = new AIScript(1L, "Meg", "turnRight turnLeft");
        when(mockRepository.findByNameIgnoreCase("Meg")).thenReturn(inDb);

        // assert
        NameUnavailableException exception = assertThrows(NameUnavailableException.class, () -> {
            // act
            service.saveAI(new AIScript(null, "Meg", "jump jump"));
        });
        // assert
        assertEquals("An AI Script with that name already exists.", exception.getMessage());
    }

    @Test
    public void saveAI_TrimmedNameAlreadyExists_ShouldThrowError() {
        // arrange
        AIScript inDb = new AIScript(1L, "Meg", "turnRight turnLeft");
        when(mockRepository.findByNameIgnoreCase("Meg")).thenReturn(inDb);

        // assert
        NameUnavailableException exception = assertThrows(NameUnavailableException.class, () -> {
            // act
            service.saveAI(new AIScript(null, "   Meg\t", "jump jump"));
        });
        // assert
        assertEquals("An AI Script with that name already exists.", exception.getMessage());
    }

    @Test
    public void saveAI_SavesAIScriptAndReturnsUpdatedAIScript() {
        //arrange
        AIScript input = new AIScript(null, "Lolita", "turnRight turnLeft");
        AIScript expected = new AIScript(25L, "Lolita", "turnRight turnLeft");
        when(mockRepository.save(input)).thenReturn(expected);

        //act
        AIScript result = service.saveAI(input);

        //assert
        // there are 2 ways of ensuring that the repository is called.
        // This is to prevent developers from hard-coding responses
        // 1. Verifying that the repository method was called.
        // 2. Running 2 test cases with different expected values.
        Mockito.verify(mockRepository).save(input);
        // To verify multiple n calls use this:
        // Mockito.verify(mockRepository, Mockito.times(n)).save(input);
        assertEquals(expected, result);
    }

    @Test
    public void getAllAI_callsRepositoryAndReturnsList() {
        //arrange
        List<AIScript> expected = Arrays.asList(
                new AIScript(null, "Meg", "jump jump"),
                new AIScript(null, "Kellsey", "move move"),
                new AIScript(null, "Lolita", "turnRight turnLeft"));
        when(mockRepository.findAll()).thenReturn(expected);

        //act
        List<AIScript> result = service.getAllAI();

        //assert
        assertEquals(expected, result);
        Mockito.verify(mockRepository).findAll();
    }

}