The gap leader and gap position calculations are performed on the server inside 

HeatStandings.java
.

Here is how the logic breaks down:

Overview of Gaps
First, the drivers are sorted into their current race positions (1st, 2nd, 3rd, etc.).

Gap Leader (gapLeader): For any given driver, this is the gap between themselves and the driver currently in 1st place. The 1st place driver's gap is always 0.0.
Gap Position (gapPosition): For any given driver, this is the gap between themselves and the driver immediately ahead of them in the standings. For example, the 3rd place driver's gap position is the gap between them and the 2nd place driver.
How the Gap is Calculated
The actual math used to compute the gap depends on how the race is being sorted/scored (the sortType):

1. Sorted by Total Time (TOTAL_TIME)
The gap is simply the difference in their total race times:

java
curDriver.getTotalTime() - leadDriver.getTotalTime()
2. Sorted by Fastest Lap (FASTEST_LAP)
The gap is the difference between their best lap times:

java
curDriver.getBestLapTime() - leadDriver.getBestLapTime()
3. Sorted by Lap Count (LAP_COUNT)
This is the most complex calculation, as it often has to estimate a time gap based on lap deficits. The logic works as follows:

Same Laps: If both drivers have the exact same number of adjusted laps, the gap is simply the difference in their total times (similar to TOTAL_TIME).
Zero Laps: If the trailing driver has 0 laps completed, the gap defaults to the leading driver's total time.
Different Laps: If the leading driver has more laps, the system calculates the lap difference (lapDiff) and then projects the time gap using the trailing driver's average lap time (avgLapTime).
If the trailing driver's average lap time is faster than the leader's, the gap is estimated as: avgLapTime * lapDiff
If the trailing driver's average lap time is slower, the gap adds the minimum between their average lap time and their total time difference, plus the projected lap gap: Math.min(avgLapTime, timeDiff) + (avgLapTime * lapDiff)
