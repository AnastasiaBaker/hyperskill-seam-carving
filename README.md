# hyperskill-seam-carving

Implementation of Seam Carving algorithm for JetBrains Academy project.

Seam carving is an algorithm for content-aware image resizing.

The program creates resized PNG image.


It runs with arguments:

"-in" It should be followed by a file name for reading the input file from the Canonical path.

"-out" It should be followed by a file name for recording the output file at the Canonical path.

"-width" It should be followed by a number of vertical seams for removing.

"-height" It should be followed by a number of horizontal seams for removing.

A vertical seam is a path of pixels connected from top to bottom in an image with one pixel in each row.
A horizontal seam is similar with the exception of the connection being from left to right.


Example:

-in test.jpg -out test-reduced.png -width 120 -height 160
