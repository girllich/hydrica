javac -cp . BitsWithProbability.java MersenneTwisterRNG.java GenCave.java && jar -cfm hydrica.jar Manifest.txt hydrica/*.class && java -jar hydrica.jar
