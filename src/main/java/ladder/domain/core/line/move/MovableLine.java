package ladder.domain.core.line.move;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import ladder.domain.core.line.Line;
import ladder.ui.result.DisplayResult;
import ladder.ui.result.DisplayResults;
import ladder.ui.result.MovablePointsDisplayResult;

import static java.util.stream.Collectors.toList;

public class MovableLine implements Line {
    static final int ERROR_INDEX_CODE = -1;
    static final int MIN_HEIGHT = 1;
    static final int MAX_HEIGHT = 10;
    static final String ERROR_MSG_OUT_OF_RANGE_HEIGHT = String.format("사다리 높이는 %d이상 %d이하로 설정되어야 합니다.", MIN_HEIGHT, MAX_HEIGHT);
    private final List<MovablePoints> lines;
    private final int maxPointCount;

    MovableLine(List<MovablePoints> lines, int maxPointCount) {
        this.lines = lines;
        this.maxPointCount = maxPointCount;
    }

    public static MovableLine of(int height, final int maxPointCount){
        verifyHeight(height);
        List<MovablePoints> movablePoints = Stream.generate(() -> MovablePoints.of(maxPointCount))
                                                  .limit(height)
                                                  .collect(toList());
        return new MovableLine(Collections.unmodifiableList(movablePoints), maxPointCount);
    }

    private static void verifyHeight(int height) {
        if (MIN_HEIGHT >= height || MAX_HEIGHT < height) {
            throw new IllegalArgumentException(ERROR_MSG_OUT_OF_RANGE_HEIGHT);
        }
    }

    static List<Integer> getLineByPointIndexPathResult(List<MovablePoints> lines, final int startIndex) {
//        AS-IS
//        int size = lines.size();
//        List<Integer> result = new ArrayList<>(size);
//        result.add(lines.get(0).move(startIndex));
//        for (int i = 1; size > i; ++i) {
//            MovablePoints line = lines.get(i);
//            result.add(line.move(result.get(i - 1)));
//        }
//        return result;

//        TO-BE
        final MovingStatefulOperation<MovablePoints, Integer> movingStatefulOperation = new MovingStatefulOperation<>(lines);
        return movingStatefulOperation.first((e) -> e.move(startIndex))
                                      .otherwise(MovablePoints::move)
                                      .states()
            ;
    }

    List<MovablePointsDisplayResult> collectMovablePointsDisplayResultList() {
        return lines.stream()
                    .map(MovablePoints::collectLinkState)
                    .map(MovablePointsDisplayResult::new)
                    .collect(toList())
            ;
    }

    @Override
    public int indexOf(int index) {
        List<Integer> lineByPointIndexPathResult = getLineByPointIndexPathResult(lines, index);
        int lastIndex = lineByPointIndexPathResult.size() - 1;
        return lineByPointIndexPathResult
            .stream()
            .skip(lastIndex)
            .findAny()
            .orElse(ERROR_INDEX_CODE);
    }

    @Override
    public int getWidth() {
        return maxPointCount;
    }

    @Override
    public DisplayResult toDisplayResult() {
        return new DisplayResults(collectMovablePointsDisplayResultList());
    }
}