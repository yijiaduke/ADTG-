// package edu.duke.adtg.domain;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;

// import java.time.LocalDate;
// import java.time.format.DateTimeFormatter;

// import static org.junit.jupiter.api.Assertions.assertEquals;

// public class SectionTest {

//     private Section section;

//     @BeforeEach
//     void setUp() {
//         section = new Section();
//     }

//     @Test
//     void testGetSemesterYear() {
//         // Set the date to April 1, 2022
//         LocalDate date = LocalDate.of(2022, 4, 1);
//         section.setDate(date);

//         // Calculate the expected semester year
//         String expectedSemesterYear = "s22";

//         // Call the method and assert the result
//         String actualSemesterYear = section.getSemesterYear();
//         assertEquals(expectedSemesterYear, actualSemesterYear);

//         LocalDate date2 = LocalDate.of(2022, 8, 1);
//         section.setDate(date2);

//         // Calculate the expected semester year
//         String expectedSemesterYear2 = "f22";

//         // Call the method and assert the result
//         String actualSemesterYear2 = section.getSemesterYear();
//         assertEquals(expectedSemesterYear2, actualSemesterYear2);

//         LocalDate date3 = LocalDate.of(2022, 6, 1);
//         section.setDate(date3);

//         // Calculate the expected semester year
//         String expectedSemesterYear3 = "su22";

//         // Call the method and assert the result
//         String actualSemesterYear3 = section.getSemesterYear();
//         assertEquals(expectedSemesterYear3, actualSemesterYear3);
//     }
// }