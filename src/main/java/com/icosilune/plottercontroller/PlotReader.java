/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller;

import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;
import com.google.common.primitives.Doubles;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author ashmore
 */
public class PlotReader {

  // Represents the ordering of data channels in the CSV file.
  // The first column should always be the stroke index.
  private final List<DataChannel> channelOrdering;

  public PlotReader(List<DataChannel> channelOrdering) {
    this.channelOrdering = channelOrdering;
  }

  public Plot read(Reader reader) throws IOException {
    CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
    
    List<Stroke> strokes = new ArrayList<>();

    int currentStrokeIndex = 0;
    RowSortedTable<Integer, DataChannel, Double> strokeData = TreeBasedTable.create();
    
    int currentRow = 0;

    for (CSVRecord record : csvParser.getRecords()) {
      int strokeIndex = Integer.valueOf(record.get(0));

      if (strokeIndex != currentStrokeIndex) {
        // increment?
        strokes.add(createStroke(strokeData));
        strokeData.clear();
        currentStrokeIndex++;
        currentRow = 0;
      }

      for(int i = 0; i< channelOrdering.size(); i++) {
        strokeData.put(currentRow, channelOrdering.get(i), Double.valueOf(record.get(i+1)));
      }
    }
    return Plot.create(strokes);
  }

  private static Stroke createStroke(RowSortedTable<Integer, DataChannel, Double> strokeData) {
    return new Stroke(strokeData.columnMap().entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey, x -> convertData((SortedMap<Integer, Double>) x.getValue()))));
  }

  private static double[] convertData(SortedMap<Integer, Double> data) {
    return Doubles.toArray(data.values());
  }
}
