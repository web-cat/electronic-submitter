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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Pattern;
import org.webcat.submitter.targets.AssignmentTarget;

//--------------------------------------------------------------------------
/**
 * This class collects references to a number of objects that are required in
 * various places during the submission process, so that they can be easily
 * passed between functions.
 *
 * @author  Tony Allevato (Virginia Tech Computer Science)
 * @author  latest changes by: $Author$
 * @version $Revision$ $Date$
 */
public class SubmissionManifest
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets the assignment referred to by this object.
     *
     * @return an AssignmentTarget representing the assignment to submit
     */
    public AssignmentTarget getAssignment()
    {
        return assignment;
    }


    // ----------------------------------------------------------
    /**
     * Sets the assignment referred to by this object.
     *
     * @param value an AssignmentTarget representing the assignment to submit
     */
    public void setAssignment(AssignmentTarget value)
    {
        assignment = value;
    }


    // ----------------------------------------------------------
    /**
     * Gets the submittable items that represent the resources to be submitted.
     *
     * @return An array of ISubmittableItems representing the files to submit
     */
    public ISubmittableItem[] getSubmittableItems()
    {
        return submittables;
    }


    // ----------------------------------------------------------
    /**
     * Sets the submittable items that represent the resources to be submitted.
     *
     * @param value an array of ISubmittableItems representing the files to
     *     submit
     */
    public void setSubmittableItems(ISubmittableItem[] value)
    {
        submittables = value;
    }


    // ----------------------------------------------------------
    /**
     * A convenience method for setting the array of submittable items to a
     * single item.
     *
     * @param value a single ISubmittableItem representing the files to
     *     submit
     */
    public void setSubmittableItems(ISubmittableItem value)
    {
        setSubmittableItems(new ISubmittableItem[] { value });
    }


    // ----------------------------------------------------------
    /**
     * Gets the username referred to by this object.
     *
     * @return a String containing the username
     */
    public String getUsername()
    {
        return username;
    }


    // ----------------------------------------------------------
    /**
     * Sets the username referred to by this object.
     *
     * @param value a String containing the username
     */
    public void setUsername(String value)
    {
        username = value;
    }


    // ----------------------------------------------------------
    /**
     * Gets the password referred to by this object.
     *
     * @return a String containing the password
     */
    public String getPassword()
    {
        return password;
    }


    // ----------------------------------------------------------
    /**
     * Sets the password referred to by this object.
     *
     * @param value a String containing the password
     */
    public void setPassword(String value)
    {
        password = value;
    }


    // ----------------------------------------------------------
    /**
     * Resolves the specified parameter string by replacing any variable
     * placeholders with their actual values. Currently the following
     * placeholders are supported:
     *
     * <ul>
     * <li>${user} - the username</li>
     * <li>${pw} - the password</li>
     * <li>${assignment.name} - the name of the assignment</li>
     * </ul>
     *
     * @param value the String containing placeholders to be resolved
     * @return a copy of the original string with the placeholders replaced by
     *     their actual values
     */
    public String resolveParameters(String value)
    {
        return resolveParameters(value, false);
    }


    // ----------------------------------------------------------
    /**
     * Identical to {@link #resolveParameters(String)}, but with the added
     * option that the password can be protected to display merely
     * "&lt;PASSWORD&gt;" instead of the user's actual password. This method
     * should be used if a parameter value is to be displayed to the user or
     * logged.
     *
     * @param value the String containing placeholders to be resolved
     * @param protectPassword true to protect the password, otherwise false
     * @return a copy of the original string with the placeholders replaced by
     *     their actual values
     */
    public String resolveParameters(String value, boolean protectPassword)
    {
        if (value != null)
        {
            Pattern pattern;

            if (username != null)
            {
                pattern = Pattern.compile("\\$\\{user\\}");
                value = pattern.matcher(value).replaceAll(username);
            }

            if (password != null)
            {
                pattern = Pattern.compile("\\$\\{pw\\}");
                value = pattern.matcher(value).replaceAll(
                        protectPassword ? "<PASSWORD>" : password);
            }

            pattern = Pattern.compile("\\$\\{assignment\\.name\\}");
            String asmtName = assignment.getName().replaceAll(" ", "%20");
            value = pattern.matcher(value).replaceAll(asmtName);
        }

        return value;
    }


    // ----------------------------------------------------------
    /**
     * Gets a URI that represents the transport to use for submitting the
     * package, once any parameter placeholders have been resolved.
     *
     * @return the URL to submit the package to
     * @throws SubmissionTargetException if an exception occurs
     */
    public URI getResolvedTransport() throws SubmissionTargetException
    {
        String uriString = resolveParameters(assignment.getTransport());

        try
        {
            return new URI(uriString);
        }
        catch (URISyntaxException e)
        {
            throw new SubmissionTargetException(e);
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets a URI that represents the transport to use for submitting the
     * package, once any parameter placeholders have been resolved, while also
     * protecting the user's password from being displayed. This method is
     * useful for logging.
     *
     * @return the URL to submit the package to
     * @throws SubmissionTargetException if an exception occurs
     */
    public URI getResolvedTransportWithoutPassword()
    throws SubmissionTargetException
    {
        String uriString = resolveParameters(assignment.getTransport(), true);

        try
        {
            return new URI(uriString);
        }
        catch (URISyntaxException e)
        {
            throw new SubmissionTargetException(e);
        }
    }


    //  ---------------------------------------------------------
    /**
     * Takes the forest of submittable items and sends those that should be
     * included (based on the target assignment) to the specified stream,
     * using the packager defined for the target assignment.
     *
     * Users implementing a user interface for this submitter core need
     * not call this method; rather, it is exposed as public so that
     * custom protocol handlers can call it from within their
     * {@link IProtocol#submit(SubmissionManifest, ILongRunningTask)} method in
     * order to transfer the final submitted package to its destination.
     *
     * @param stream the OutputStream to write the package contents to
     * @param task the long-running task to be used to notify the user of the
     *     progress of the operation
     *
     * @throws IOException if an I/O exception occurred
     */
    public void packageContentsIntoStream(OutputStream stream,
            ILongRunningTask task) throws IOException
    {
        task.beginSubtask(3);

        final IPackager packager =
            PackagerRegistry.getInstance().createPackagerInstance(
                    assignment.getPackager());

        if (packager == null)
        {
            throw new PackagerNotRegisteredException(assignment.getPackager());
        }

        Map<String, String> parameters = assignment.getPackagerParameters();
        Map<String, String> resolvedParameters =
            new Hashtable<String, String>();

        for (String key : parameters.keySet())
        {
            String value = parameters.get(key);
            String resolved = resolveParameters(value);

            resolvedParameters.put(key, resolved);
        }

        packager.startPackage(stream, resolvedParameters);
        task.doWork(1);

        LongRunningSubmittableItemVisitor visitor =
            new LongRunningSubmittableItemVisitor(task)
        {
            protected void accept(ISubmittableItem item)
            throws InvocationTargetException
            {
                try
                {
                    if (!assignment.isFileExcluded(item.getFilename()))
                    {
                        packager.addSubmittableItem(item);
                        Thread.sleep(500);
                    }
                }
                catch (Exception e)
                {
                    throw new InvocationTargetException(e);
                }
            }
        };

        try
        {
            // This visitation acts as the second unit of work so manually
            // incrementing the task here is not required.

            visitor.visit(submittables);
        }
        catch (InvocationTargetException e)
        {
            if (e.getCause() instanceof IOException)
            {
                throw (IOException) e.getCause();
            }
            else
            {
                e.getCause().printStackTrace();
            }
        }

        packager.endPackage();
        task.doWork(1);

        task.finishSubtask();
    }


    //~ Static/instance variables .............................................

    /* The assignment to which the user is submitting. */
    private AssignmentTarget assignment;

    /* The submittable items to be packaged and submitted. */
    private ISubmittableItem[] submittables;

    /* The ID of the user. */
    private String username;

    /* The password used to log into the submission target system, if
       required. */
    private String password;
}
