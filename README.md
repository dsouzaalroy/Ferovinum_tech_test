
# Report
## Description

The CSV file provided is parsed and converted into a nested Hashmap. Each cell is iterated over and the final value of the postfix expression is calculated and saved back into the cell. The calculation of the postfix expression is done using a stack. The value of each cell is either looked up right away or recursively found. The code ensures that the edge cases are considered and, places a '#ERR' in the cell if one is found.

## Limitations
Every cell is evaluated atleast once. 

The code fails if the csv file is not in the correct format (e.g Tab separated instead of comma separated).

## Trade offs or design decisions
Nested Hashmap - The code will at the minimum look up each cell at least once. Additionally, if a postfix expression is found an insertion is made. Multiple references like (b1, c1, d1, 20) also means additional lookups and insertions. As the size of the csv file increases, there are possiblities of larger multiple reference chains which increases the number of insertions. As nested hashmaps have on average, better time complexity on insertions and lookups than arrays it has been used.

Used quite a few methods to make the code easier to read

## Edge cases
* Nested operands 1 2 3 4 5 6 7 8 9 + * / + * / + *
* Multiple references b1, c1, d1, 2 3 /
* Excessive whitespace
* Points to itself a1
* Division by zero 0 0 /
* Incorrect cell name (aa)
* Non existent cell d999
* Capital Letters for cell name A2
* No operators 1 2 3
* No numbers + / -
* Circular reference b1, c1, a1
* Numbers cannot have an operator before them (without whitespace) +7 -7
