/*
 * Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Copyright (c) 2004-2009 Paul R. Holser, Jr.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jdk.joptsimple.internal;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import static java.lang.Integer.*;
import static java.lang.System.*;
import static java.util.Collections.*;

import static jdk.joptsimple.internal.Column.*;
import static jdk.joptsimple.internal.Strings.*;

/**
 * <p>A means to display data in a text grid.</p>
 *
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 * @version $Id: ColumnarData.java,v 1.13 2008/12/17 04:10:57 pholser Exp $
 */
public class ColumnarData {
    private static final String LINE_SEPARATOR = getProperty( "line.separator" );
    private static final int TOTAL_WIDTH = 80;

    private final ColumnWidthCalculator widthCalculator;
    private final List<Column> columns;
    private final String[] headers;

    /**
     * Creates a new grid with the given column headers.
     *
     * @param headers column headers
     */
    public ColumnarData( String... headers ) {
        this.widthCalculator = new ColumnWidthCalculator();
        this.columns = new LinkedList<Column>();
        this.headers = headers.clone();

        clear();
    }

    /**
     * Adds a row to the grid.  The data will fall under the corresponding headers.
     * There can be fewer elements in the row than headers.  Any data in columns outside
     * of the number of headers will not be added to the grid.
     *
     * @param rowData row data to add
     */
    public void addRow( Object... rowData ) {
        int[] numberOfCellsAddedAt = addRowCells( rowData );
        addPaddingCells( numberOfCellsAddedAt );
    }

    /**
     * Gives a string that represents the data formatted in columns.
     *
     * @return the formatted grid
     */
    public String format() {
        StringBuilder buffer = new StringBuilder();

        writeHeadersOn( buffer );
        writeSeparatorsOn( buffer );
        writeRowsOn( buffer );

        return buffer.toString();
    }

    /**
     * Removes all data from the grid, but preserves the headers.
     */
    public final void clear() {
        columns.clear();

        int desiredColumnWidth = widthCalculator.calculate( TOTAL_WIDTH, headers.length );
        for ( String each : headers )
            columns.add( new Column( each, desiredColumnWidth ) );
    }

    private void writeHeadersOn( StringBuilder buffer ) {
        for ( Iterator<Column> iter = columns.iterator(); iter.hasNext(); )
            iter.next().writeHeaderOn( buffer, iter.hasNext() );

        buffer.append( LINE_SEPARATOR );
    }

    private void writeSeparatorsOn( StringBuilder buffer ) {
        for ( Iterator<Column> iter = columns.iterator(); iter.hasNext(); )
            iter.next().writeSeparatorOn( buffer, iter.hasNext() );

        buffer.append( LINE_SEPARATOR );
    }

    private void writeRowsOn( StringBuilder buffer ) {
        int maxHeight = max( columns, BY_HEIGHT ).height();

        for ( int i = 0; i < maxHeight; ++i )
            writeRowOn( buffer, i );
    }

    private void writeRowOn( StringBuilder buffer, int rowIndex ) {
        for ( Iterator<Column> iter = columns.iterator(); iter.hasNext(); )
            iter.next().writeCellOn( rowIndex, buffer, iter.hasNext() );

        buffer.append( LINE_SEPARATOR );
    }

    private int arrayMax( int[] numbers ) {
        int maximum = MIN_VALUE;

        for ( int each : numbers )
            maximum = Math.max( maximum, each );

        return maximum;
    }

    private int[] addRowCells( Object... rowData ) {
        int[] cellsAddedAt = new int[ rowData.length ];

        Iterator<Column> iter = columns.iterator();
        for ( int i = 0; iter.hasNext() && i < rowData.length; ++i )
            cellsAddedAt[ i ] = iter.next().addCells( rowData[ i ] );

        return cellsAddedAt;
    }

    private void addPaddingCells( int... numberOfCellsAddedAt ) {
        int maxHeight = arrayMax( numberOfCellsAddedAt );

        Iterator<Column> iter = columns.iterator();
        for ( int i = 0; iter.hasNext() && i < numberOfCellsAddedAt.length; ++i ) {
            addPaddingCellsForColumn(
                iter.next(), maxHeight, numberOfCellsAddedAt[ i ] );
        }
    }

    private void addPaddingCellsForColumn( Column column, int maxHeight,
        int numberOfCellsAdded ) {

        for ( int i = 0; i < maxHeight - numberOfCellsAdded; ++i )
            column.addCell( EMPTY );
    }
}
