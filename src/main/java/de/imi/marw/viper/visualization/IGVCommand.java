/* Copyright (c) 2017 Marius Wöste
 *
 * This file is part of VIPER.
 *
 * VIPER is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VIPER is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VIPER.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.imi.marw.viper.visualization;

import java.util.Objects;

/**
 *
 * @author marius
 */
public class IGVCommand implements Comparable<IGVCommand> {

    private final String key;
    private final String[] subCommands;
    private final Runnable finishedCallback;
    private final boolean urgent;

    public IGVCommand(String key, String[] subCommands, boolean urgent, Runnable finishedCallback) {
        this.subCommands = subCommands;
        this.finishedCallback = finishedCallback;
        this.key = key;
        this.urgent = urgent;
    }

    public Runnable getFinishedCallback() {
        return finishedCallback;
    }

    public String[] getSubCommands() {
        return subCommands;
    }

    public String getKey() {
        return key;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.key);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IGVCommand other = (IGVCommand) obj;
        return Objects.equals(this.key, other.key);
    }

    @Override
    public int compareTo(IGVCommand o) {
        return Boolean.compare(this.urgent, o.urgent);
    }
}
