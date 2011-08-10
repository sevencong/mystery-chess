/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mysterychess.util;

/**
 *
 * @author Tin Bui-Huy
 */
public interface Task {
    public void perform() throws Exception;

    public String getDescription();
}
