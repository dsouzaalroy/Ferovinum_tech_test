
# Report
## Description

The code takes in the files path as an argument. The CSV file is then parsed into a ArrayList<String[]>. Each cell is then validated using a Stack to check if the postfix expression is valid. Each whitespace separated character is also validated(e.g. only a1, 10). The final value of each character is found using getValue() to get the numerical value if the character is pointing to another cell. Once the entire expression is computed without an error the computed value is added back to the list. Finally, once the entire list is iterated, the contents of the list are read and output to the terminal.

## Limitations
The code cannot evaluate a cell after it (e.g. |b1 2 +|20|).

The code fails if the csv file is not in the correct format (e.g Tab separated instead of comma separated).

## Trade offs or design decisions
ArrayList<String[]> was used instead of String[][], because it was uncertain that the rows could be of a fixed length or varied length

Used quite a few methods to make the code easier to read

## Edge cases
Points to itself

Division by zero

Incorrect cell name (aa)

Non existent cell

Capital Letters for cell name

No operators

No numbers
