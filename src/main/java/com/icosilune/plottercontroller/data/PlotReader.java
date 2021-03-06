/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icosilune.plottercontroller.data;

import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;
import com.google.common.primitives.Doubles;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author ashmore
 */
public class PlotReader {
  private static final Logger LOG = Logger.getLogger( PlotReader.class.getName() );

  // Represents the ordering of data channels in the CSV file.
  // The first column should always be the stroke index.
  private final List<DataChannel> channelOrdering;

  public PlotReader(List<DataChannel> channelOrdering) {
    this.channelOrdering = channelOrdering;
  }

  public Plot read(Reader reader) throws IOException, ParseException {
    CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
    LOG.info("Reading plot");
    
    List<Stroke> strokes = new ArrayList<>();

    int currentStrokeIndex = 0;
    RowSortedTable<Integer, DataChannel, Double> strokeData = TreeBasedTable.create();
    
    int currentRow = 0;
    
    try {

    for (CSVRecord record : csvParser.getRecords()) {
      int strokeIndex = Integer.valueOf(trimEntry(record.get(0)));

      if (strokeIndex != currentStrokeIndex) {
        // increment?
        strokes.add(createStroke(strokeData));
        strokeData.clear();
        currentStrokeIndex++;
        currentRow = 0;
      }

      for(int i = 0; i<channelOrdering.size(); i++) {
        Double value = Double.valueOf(trimEntry(record.get(i+1)));
        DataChannel channel = channelOrdering.get(i);
        strokeData.put(currentRow, channel, value);
      }
      currentRow++;
    }
    strokes.add(createStroke(strokeData));
    
    } catch (ArrayIndexOutOfBoundsException | NoSuchElementException ex) {
      throw new ParseException(ex);
    }
    
    
    LOG.log(Level.INFO, "Read plot with {0} strokes", strokes.size());
    for (int i=0;i<strokes.size();i++) {
      LOG.log(Level.INFO, "Stroke {0} has {1} data points", new Object[]{i, strokes.get(i).getNumberDataPoints()});
    }
    
    return new Plot(strokes);
  }
  
  public class ParseException extends Exception {
    public ParseException(Throwable cause) {
      super(cause);
    }
  }
  
  private String trimEntry(String s) {
    // remove stuff that might be left over from Rhino
    return s.replaceAll("[\\(\\)]", "");
  }

  private static Stroke createStroke(RowSortedTable<Integer, DataChannel, Double> strokeData) {
    return new Stroke(strokeData.columnMap().entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey, x -> convertData(x.getValue()))));
  }

  private static double[] convertData(Map<Integer, Double> data) {
    return Doubles.toArray(data.values());
  }
}
