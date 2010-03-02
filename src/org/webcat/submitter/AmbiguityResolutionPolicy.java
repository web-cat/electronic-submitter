/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2009 Virginia Tech
 |
 |  This file is part of Web-CAT Electronic Submitter.
 |
 |  Web-CAT is free software; you can redistribute it and/or modify
 |  it under the terms of the GNU General Public License as published by
 |  the Free Software Foundation; either version 2 of the License, or
 |  (at your option) any later version.
 |
 |  Web-CAT is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU General Public License for more details.
 |
 |  You should have received a copy of the GNU General Public License along
 |  with Web-CAT; if not, see <http://www.gnu.org/licenses/>.
\*==========================================================================*/

package org.webcat.submitter;

//--------------------------------------------------------------------------
/**
 * Specifies how a file should be treated if it satisfies both an inclusion and
 * exclusion directive at the same level of the submission target tree.
 *
 * @author  Tony Allevato (Virginia Tech Computer Science)
 * @author  latest changes by: $Author$
 * @version $Revision$ $Date$
 */
public enum AmbiguityResolutionPolicy
{
    // ----------------------------------------------------------
    /**
     * Specifies that a file should be included in the event that it satisfies
     * an &lt;include&gt; and &lt;exclude&gt; directive at the same level of the
     * tree.
     */
    INCLUDE,


    // ----------------------------------------------------------
    /**
     * Specifies that a file should be excluded in the event that it satisfies
     * an &lt;include&gt; and &lt;exclude&gt; directive at the same level of the
     * tree.
     */
    EXCLUDE
}
