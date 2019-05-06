# Pen and Paper
Pen and Paper uses the transportation algorithm to assign alumni to prospective students based on their compatibility and attributes of the student. These alumni encourage their assigned prospective students to attend university with written letters. Learn more [here](https://drive.google.com/file/d/1EoF5_zd4GI40i5o1kP3NXk4aGLSXSei2/view?usp=sharing).

## To Run
1. Download and unzip this Github repository.

![alt text](https://github.com/DevonFulcher/PenAndPaper/blob/master/resources/download.png "download")

2. [Install](https://www.java.com/en/download/) Java.
3. [Install](https://www.gnu.org/software/glpk/) GLPK which contains GLPSOL.
4. Create a survey in [this](https://forms.gle/xVXEqm7ok8pMgQ9u5) style and send to alumni volunteers. Once the volunteers have submitted their answers, download their responses as a .csv file, rename that file to Alumni Data.csv, and place it in the [confidential_data](https://github.com/DevonFulcher/PenAndPaper/tree/master/confidential_data) folder. Be sure to remove all duplicates and unfinished forms from this file.

![alt text](https://github.com/DevonFulcher/PenAndPaper/blob/master/resources/access_google_sheet.png "access_google_sheet")

![alt text](https://github.com/DevonFulcher/PenAndPaper/blob/master/resources/google_sheet_to_csv.png "google_sheet_to_csv")

5. If applicable, edit Majors List.csv and Scholarship List.csv in [open_data](https://github.com/DevonFulcher/PenAndPaper/tree/master/open_data).
6. Insert correctly formatted Student Data.csv in [confidential_data](https://github.com/DevonFulcher/PenAndPaper/tree/master/confidential_data). Here is what each column should contain: 

Ref | Gender | 1st Gen | Region | Scholarship Level | Conversion Predictor | Postal | Academic Interest | Extracurricular Interest 
--- | ---| --- | --- | --- | --- | --- | ---| --- 
unique identifier | M or F for gender identity (for gendered sports matching) | 1 or blank for 1st generation student status | string of student's location (TX if in state status)| items in Scholarship List.csv | predicted enrollment (1 is more likely and 6 is less likely) | zip code | comma separated list of items in Majors List.csv | comma separated strings 

Here is an example of the format of Student Data.csv: 

![alt text](https://github.com/DevonFulcher/PenAndPaper/blob/master/resources/students.png "students")

To change a .xlsx file to a .csv file, open Excel, go to File, Save As, then save with the "CSV UTF-8 (Comma delimited)(*.csv)" extension.

![alt text](https://github.com/DevonFulcher/PenAndPaper/blob/master/resources/xlsx_to_csv.png "xlsx_to_csv")

7. Double-click Start.bat and follow instruction prompts.
8. Retrieve results in the [results](https://github.com/DevonFulcher/PenAndPaper/tree/master/results) folder.

## Interpreting Results
This program creates 4 output files to interpret.
* Category Analysis.csv: Provides statistics on the matchings that this program made.
* Final Output.csv: Provides the matchings between students and alumni. Each matching is listed with the reason that matching was made and, if applicable, the student and alumni information that attributed to that matching.
* Match Scores.csv: A matrix showing all match scores between each alumnus and student. A match score is the value of an assignment of an alumnus to student.
* Students Lacking Letters.csv: A list of all students who did not receive letters, sorted by their average match score.

## Acknowledgements
Pen and Paper was developed for the Fall 2018 Operations Research course and Spring 2019 Operations Research in Practive independent study at Southwestern Univeristy. [Dr. Barbara Anthony's](https://www.southwestern.edu/live/profiles/25773-barbara-m-anthony) advised this project. This project was completed with help from [Daniel Maldonaldo](https://www.linkedin.com/in/daniel-maldonado-468292128/), [Katie Dyo](https://www.linkedin.com/in/katiedyo/), [Alexander Hoffman](https://www.linkedin.com/in/alexander-hoffman-bb3aa2134/), and [Greg O'brien](https://www.linkedin.com/in/gregoryobrien1613/).
