//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package de.latlon.deejump.base.ui;

import static java.awt.GridBagConstraints.WEST;
import static java.util.Arrays.asList;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * <code>CSVSelectionPanel</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CSVSelectionPanel extends JPanel {

    private static final long serialVersionUID = 6912663538834912547L;

    private JRadioButton[] xboxes, yboxes;

    /**
     * @param header
     * 
     */
    public CSVSelectionPanel( List<String[]> header ) {
        if ( header.isEmpty() || header.get( 0 ).length < 2 ) {
            throw new IllegalArgumentException( "The number of csv colums must be at least two." );
        }

        setLayout( new GridBagLayout() );
        GridBagConstraints gb = new GridBagConstraints();
        gb.insets = new Insets( 5, 5, 5, 5 );
        gb.gridx = 0;
        gb.gridy = 0;

        setupButtons( gb, header );

        ++gb.gridy;
        gb.gridx = 1;

        // add the values from the header
        for ( String[] line : header ) {
            ++gb.gridy;
            gb.gridx = 1;
            for ( String s : line ) {
                add( new JLabel( s ), gb );
                ++gb.gridx;
            }
        }
    }

    private void setupButtons( GridBagConstraints gb, List<String[]> header ) {
        xboxes = new JRadioButton[header.get( 0 ).length];

        gb.anchor = WEST;
        add( new JLabel( de.latlon.deejump.base.i18n.I18N.get( "CSVSelectionPanel.useasx" ) ), gb );
        ++gb.gridx;

        for ( int i = 0; i < xboxes.length; ++i ) {
            xboxes[i] = new JRadioButton();
            add( xboxes[i], gb );
            ++gb.gridx;
        }

        ++gb.gridy;
        gb.gridx = 0;
        yboxes = new JRadioButton[header.get( 0 ).length];
        add( new JLabel( de.latlon.deejump.base.i18n.I18N.get( "CSVSelectionPanel.useasy" ) ), gb );
        ++gb.gridx;

        for ( int i = 0; i < yboxes.length; ++i ) {
            yboxes[i] = new JRadioButton();
            add( yboxes[i], gb );
            ++gb.gridx;
        }

        ActionListener listener = getXYListener( xboxes, yboxes );
        ButtonGroup group = new ButtonGroup();
        for ( JRadioButton b : xboxes ) {
            b.addActionListener( listener );
            group.add( b );
        }

        group = new ButtonGroup();
        for ( JRadioButton b : yboxes ) {
            b.addActionListener( listener );
            group.add( b );
        }

        xboxes[0].setSelected( true );
        yboxes[1].setSelected( true );
    }

    private static ActionListener getXYListener( final JRadioButton[] bs1, final JRadioButton[] bs2 ) {
        return new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
                HashSet<JRadioButton> bset1 = new HashSet<JRadioButton>( asList( bs1 ) );
                HashSet<JRadioButton> bset2 = new HashSet<JRadioButton>( asList( bs2 ) );

                Object src = evt.getSource();

                if ( bset1.contains( src ) ) {
                    fixSelection( src, bs1, bs2 );
                }
                if ( bset2.contains( evt.getSource() ) ) {
                    fixSelection( src, bs2, bs1 );
                }
            }
        };
    }

    static void fixSelection( Object src, JRadioButton[] bs1, JRadioButton[] bs2 ) {
        for ( int i = 0; i < bs1.length; ++i ) {
            if ( bs1[i] == src ) {
                if ( bs2[i].isSelected() ) {
                    if ( i - 1 > -1 ) {
                        bs2[i - 1].setSelected( true );
                    } else {
                        bs2[i + 1].setSelected( true );
                    }
                }
            }
        }
    }

    /**
     * @return the selected x column index
     */
    public int getXColumn() {
        for ( int i = 0; i < xboxes.length; ++i ) {
            if ( xboxes[i].isSelected() ) {
                return i;
            }
        }

        return -1;
    }

    /**
     * @return the selected y column index
     */
    public int getYColumn() {
        for ( int i = 0; i < yboxes.length; ++i ) {
            if ( yboxes[i].isSelected() ) {
                return i;
            }
        }

        return -1;
    }

}
