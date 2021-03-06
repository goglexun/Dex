package com.dexvis.dex

import com.dexvis.dex.exception.DexException
import com.dexvis.util.DateUtil


/**
 * 
 * This is the main structure to hold Dex Data.
 * 
 * @author Patrick Martin
 * 
 */
public class DexData {
  // Header and data:
  def List<String>       header
  def List<List<String>> data
  def List<String> types;
  /**
   * 
   * Construct an empty instance of DexData.
   * 
   */
  public DexData()
  {
    this(new ArrayList<String>())
  }
  
  /**
   * 
   * Given a header, construct a DexData structure with no data.
   * 
   * @param header
   *          The initial header.
   * 
   */
  def DexData(List<String> header)
  {
    this(header, new ArrayList<List<String>>())
  }

  /**
   *
   * Given a header and some data, construct a DexData structure to match.
   *
   * @param header The header.
   * @param data The data.
   *
   */
  def DexData(List<String> header, List<List<String>> data)
  {
    this(header, data, new ArrayList<List<String>>())
  }

  /**
   * 
   * Given a header, some data, and a list of types, construct a DexData structure to match.
   * 
   * @param header The header.
   * @param data The data.
   * @param types The type of each header.
   * 
   */
  def DexData(List<String> header, List<List<String>> data, List<String> types)
  {
    this.header = header.collect()
    this.data = new ArrayList<List<String>>()
    this.types = new ArrayList<List<String>>()
    
    data.each {
      row ->
      this.data << row.collect()
    }
    
    types.each {
      row ->
      this.types << row
    }
  }

  /**
   *
   * Given a header and some data, construct a DexData structure to match.
   *
   * @param header The header.
   * @param data The data.
   *
   */
  def DexData(DexData dex)
  {
    this(dex.header, dex.data)
  }
  
  /**
   * 
   * Return an alternate structure map where each column is contained in the
   * key, and the value of the map is the list of row entries for that column.
   * 
   * @return A map where column names are the keys into the map and the values
   *         are the rows for that each column name.
   * 
   */
  public Map<String, List<String>> getColumnMap()
  {
    Map<String, List<String>> columnMap = new HashMap<String, List<String>>()
    int numRows = data.size()
    int numCols = header.size()
    for (String colName : header)
    {
      columnMap.put(colName, new ArrayList<String>())
    }
    
    for (int rowNum = 0; rowNum < numRows; rowNum++)
    {
      for (int colNum = 0; colNum < numCols; colNum++)
      {
        try
        {
          columnMap.get(header.get(colNum)).add(data.get(rowNum).get(colNum))
        }
        catch (Exception ex)
        {
          throw new DexException("Accessing: COLNAME: ${header.get(colNum)}, COL=$colNum, ROW: $rowNum, HEADER=$header")
        }
      }
    }
    
    return columnMap
  }
  
  public List<Map<String, String>> getMapList()
  {
    List<Map<String, String>> list = new ArrayList<Map<String, String>>();
    
    data.eachWithIndex { row, ri ->
      
      Map<String, String> rowMap = new HashMap<String, String>();
      
      header.eachWithIndex { hdr, hi ->
        rowMap[hdr] = row[hi]
      }
      
      list << rowMap
    }
    
    return list;
  }
  
  /**
   * 
   * Given a column name, find it's column number.
   * 
   * @param name
   *          The column name whose index we are searching.
   * 
   * @return The index of the column or -1 if the column index is not found.
   * 
   */
  public int getColumnNumber(String name)
  {
    println "Searching for: '$name' in '$header'"
    header.eachWithIndex
    { h, hi ->
      //println "header[$hi]=$h which is a ${h.getClass()}"
    }
    return header.indexOf(name.toString())
  }
  
  /**
   * 
   * Given a column name, return it's row contents as a string list.
   * 
   * @param name The name of the column we are seeking.
   * 
   * @return The column's contents as a string list.
   * 
   */
  public List<String> getColumn(String name)
  {
    //println "HEADER: $header"
    //println "getColumn($name) index=${header.findIndexOf() { it==name }}"
    return getColumn(header.findIndexOf() {it==name})
  }
  
  /**
   * 
   * Given a column index, return it's row contents as a string list.
   * 
   * @param colIndex The index of the column whose row contents we seek.
   * 
   * @return The column's contents as a string list.
   * 
   */
  public List<String> getColumn(int colIndex)
  {
    List<String> column = null
    
    if (colIndex >= 0 && colIndex < header.size())
    {
      column = new ArrayList<String>()
      for (List<String> row : data)
      {
        column.add(row.get(colIndex))
      }
    }
    return column
  }
  
  /**
   * 
   * Return a list of names of all numeric columns.
   * 
   * @return A list of column names which are numerics.
   * 
   */
  public List<String> getNumericColumns()
  {
    List<String> numericColumns = new ArrayList<String>()
    double d
    
    for (int colNum = 0; colNum < header.size(); colNum++)
    {
      try
      {
        for (int rowNum = 0; rowNum < data.size(); rowNum++)
        {
          d = Double.parseDouble(data.get(rowNum).get(colNum))
        }
        numericColumns.add(header.get(colNum))
      }
      catch(Exception ex)
      {
        //ex.printStackTrace();
      }
    }
    
    return numericColumns
  }
  
  public DexData getNumericData()
  {
    return select(getNumericColumns())
  }
  
  public DexData select(List<String> columns)
  {
    def newHeader = columns.findAll
    { col ->
      (col as String) in header
    }
    
    //println "select($columns) from $header = $newHeader"
    if (newHeader.size() == 0)
    {
      return new DexData()
    }
    
    def indices = newHeader.collect { header.indexOf(it) }
    
    DexData newDexData = new DexData(newHeader, data.collect { it[indices] })
    //println "DEX RETURNS: $newDexData"
    return newDexData
  }
  
  /**
   * 
   * Change the name of header[hi] to name.
   * 
   * @param hi
   * @param name
   */
  public void renameHeader(int hi, String name)
  {
    header.set(hi, name);
  }
  
  /**
   * 
   * Return the column named colName as a list of Doubles.  If the column
   * can't be coerced into a Double, set it to a default value of 0.
   * 
   * @param colName The column we are retrieving and converting.
   * 
   * @return An array of doubles representing our best effort at converting
   * to double.
   *
   */
  def getColumnAsDouble(String colName)
  {
    return getColumnAsDouble(colName, 0)
  }
  
  /**
   *
   * Return the column named colName as a list of Doubles.  If the column
   * can't be coerced into a Double, set it to a user-supplied default value.
   *
   * @param colName The column we are retrieving and converting.
   *
   * @return An array of doubles representing our best effort at converting
   * to double.
   *
   */
  def getColumnAsDouble(String colName, Double defaultValue)
  {
    return getColumn(colName).collect {
      try
      {
        return it as Double
      }
      catch (Exception ex)
      {
        return defaultValue
      }
    }
  }
  
  /**
   * 
   * Evaluates the column as doubles and returns the minimum value.
   * 
   * @param colName The column to search.
   * 
   * @return The minimum double value within the column.  Items which can't be
   * evaluated as a double, evaluate to 0.
   * 
   */
  def min(String colName)
  {
    return getColumnAsDouble(colName).min()
  }
  
  /**
   *
   * Evaluates the column as doubles and returns the maximum value.
   *
   * @param colName The column to search.
   *
   * @return The maximum double value within the column.  Items which can't be
   * evaluated as a double, evaluate to 0.
   *
   */
  def max(String colName)
  {
    return getColumnAsDouble(colName).max()
  }
  
  def normalize(srcCol, rangeMin, rangeMax)
  {
    if (rangeMin > rangeMax)
    {
      def tmp = rangeMin
      rangeMin = rangeMax
      rangeMax = tmp
    }
    
    // Normalize to 0..1
    def range = rangeMax - rangeMin
    def rangeData = getColumnAsDouble(srcCol)
    def colMin = rangeData.min()
    def colMax = rangeData.max()
    def colRange = colMax - colMin
    
    rangeData.eachWithIndex
    { a, ri ->
      //println "RI: $ri, A: $a, ColMin: $colMin, ColMax: $colMax, RangeMin: $rangeMin, RangeMax: $rangeMax, Range: $range, ColRange: $colRange"
      
      if (range == 0 || colRange == 0)
      {
        rangeData[ri] = rangeMin
      }
      else
      {
        rangeData[ri] = ((a - colMin) / colRange) * range + rangeMin
      }
      //println "VAL: ${rangeData[ri]}"
    }
    
    rangeData.collect()
  }
  
  /**
   * 
   * Returns true if the given column exists, false otherwise.
   * 
   * @param colName The name of the column we are checking for.
   * 
   * @return True if the given column exists, false otherwise.
   * 
   */
  def columnExists(colName)
  {
    return header.indexOf(colName) >= 0
  }
  
  def getColumnIndex(colName)
  {
    return header.indexOf(colName)
  }
  
  def addColumn(String colName, int ci, List<String> colData)
  {
    int effCi = ci % (header.size());
    header.add(effCi, colName)
    
    data.eachWithIndex { row, ri ->
      println "data[$ri].add($effCi, ${colData[ri]})"
      data[ri].add(effCi, (colData[ri] as String));
    }
  }
  
  /**
   *
   * Adds a column.  If the column already exists, will replace the
   * existing contents of that column.
   *
   * @param colName The name of the column we are adding.
   * @param colData The data of the column we are adding.
   *
   */
  def addColumn(colName, colData) { return append(colName, colData) }
  
  /**
   *
   * Appends a column.  If the column already exists, will replace the
   * existing contents of that column.
   *
   * @param colName The name of the column we are adding.
   * @param colData The data of the column we are adding.
   *
   */
  def append(String colName, List<String> colData)
  {
    int columnIndex = getColumnIndex(colName)
    if (columnIndex >= 0)
    {
      colData.eachWithIndex
      { col, ci ->
        data[ci][columnIndex] = col
      }
    }
    else
    {
      colData.eachWithIndex
      { col, ci ->
        data[ci] << col
      }
      header << colName
    }
    
    this
  }
  
  /**
   * 
   * Return this dex data structure as a string.
   * 
   */
  public String toString()
  {
    String retStr = ""
    retStr = header.toString() + data.toString()
    return retStr
  }
  
  
  // COUNTRY  2000      2001
  // =======  =======   =======
  // C1       C1.2000   C1.2001
  // C2       C2.2000   C2.2001
  //
  // group("YEAR", ["2000", "2001"]) returns:
  //
  // COUNTRY  YEAR  VALUE
  // C1       2000  C1.2000
  // C1       2001  C1.2001
  // C2       2000  C2.2000
  // C2       2001  C2.2001
  public DexData groupByIndex(String groupName, String valueName, List <Integer> groupedIndices)
  {
    def ungroupedIndices = (0..(header.size()-1)) - groupedIndices
    //println "Grouped Indices: ${groupedIndices}"
    //println "Ungrouped Indices: ${ungroupedIndices}"
    def newHeader = ungroupedIndices.collect { header[it] }
    def newData = [];
    newHeader << groupName
    newHeader << valueName
    //println "New Header: ${newHeader.join(",")}"
    data.eachWithIndex { row, ri ->
      //println "ROW: $row"
      def newRowPrologue = ungroupedIndices.collect { data[ri][it] }
      groupedIndices.each { gi ->
        //println "GI: $gi"
        def newRow = newRowPrologue.collect();
        newRow << header[gi]
        newRow << row[gi]
        newData << newRow
      }
    }
    //println "New Data: $newData"
    def newDexData = new DexData(newHeader, newData);
    //println "New Dex Data: ${newDexData}"
    return newDexData
  }
  
  public DexData groupByName(String groupName, String valueName, List<String> columnNames)
  {
    // Get the indexes and group by that.
    return groupByIndex(groupName, valueName, columnNames.collect { colName -> getColumnNumber(colName)})
  }
  
  public List<Integer> getMaxLengths()
  {
    List<Integer> maxLengths = []
    
    header.eachWithIndex { hdr, ci ->
      int maxLength = 1;
      data.each { row ->
        //println "$hdr : ${row[ci] as String} = ${row[ci].length()}"
        if (row && row[ci] && row[ci].length() > maxLength)
        {
          maxLength = row[ci].length()
        }
      }
      maxLengths << maxLength;
    }
    
    return maxLengths
  }
  
  // This routine will guess basic datatypes.  It will prefer data types of:
  // Integer over Double over String.
  // Strings with valid date
  public List<String> guessTypes()
  {
    List<String> types = []
    
    header.eachWithIndex { hdr, ci ->
      
      boolean possibleInteger = true
      boolean possibleDouble = true
      boolean possibleDate = true
      
      boolean allNull = true
      boolean allEmpty = true
      
      data.eachWithIndex { row, ri ->
        if (row[ci])
        {
          allNull = false
          if (row[ci].length() > 0)
          {
            allEmpty = false
            
            if (possibleInteger)
            {
              try
              {
                row[ci] as Integer
              }
              catch (Exception ex)
              {
                println "${hdr} is not an integer due to row ${ri} containing: '${row[ci]}'}"
                possibleInteger = false
              }
            }
            
            if (possibleDouble)
            {
              try
              {
                row[ci] as Double
              }
              catch (Exception ex)
              {
                println "${hdr} is not an double due to row ${ri} containing: '${row[ci]}'}"
                possibleDouble = false
              }
            }
          }
        }
      }
      
      possibleDate = DateUtil.guessFormat(getColumn(ci)) != null
      
      // Only future-proof way to handle data of indeterminant format
      if (allNull || allEmpty)
      {
        types << "string"
      }
      else if (possibleInteger)
      {
        types << "integer"
      }
      else if (possibleDouble)
      {
        types << "double"
      }
      else if (possibleDate)
      {
        types << "date"
      }
      else
      {
        types << "string"
      }
    }
    
    return types
  }
  
  public String toCsvString()
  {
    return toCsvString("csv")
  }
  
  public String toCsvString(String name)
  {
    def csvHeader = "[" + header.collect { return "\"$it\"" }.join(',') + "]"
    def csvData = []

    data.eachWithIndex
    {
      row, ri ->
      csvData << "[" + row.collect { return "\"$it\"" }.join(',') + "]"
    }
    return "var " + name + " = { 'header' : $csvHeader,\n 'data' : [" + csvData.join(',\n') + "] };";
  }
  
  static main(args)
  {
    def dexData = new DexData(["COUNTRY", "2000", "2001"],
    [["US", "1", "2"], ["BRAZIL", "3", "4"]])
    def groupedData = dexData.groupByName("YEAR", "VALUE", ["2000", "2001"])
    println "Result: ${groupedData}"
    println "TYPES: ${dexData.guessTypes()}"
    
    //def a = [ "a", "b", "c" ].eachWithIndex { it, i -> "$i $it" }.join(",")
    //println a

    println dexData.getMaxLengths();
    
    println dexData.getMapList()
    
    //println dexData.addColumn("NA", dexData.normalize("A", 1, 10))
  }
}
