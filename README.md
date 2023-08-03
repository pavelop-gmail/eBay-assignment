# eBay's exercise: a state machine framework and a Candy Machine use case.

Code contains both scala and java implementations of state machine and tests.
Each toy candy machine implemented in a single file (events, states and machine tests)
for convenient reading.

Tested with scala 2.13.11 and Amazon Corretto 19 JDK.

To compile from console:
`./gradlew clean build`

To run Scala Candy Machine from console:
`./gradlew -PmainClass=ebay.assignment.candy.CandyMachine run`

To run Java Candy Machine from console:
`./gradlew -PmainClass=ebay.assignment.jcandy.JCandyMachine run`
