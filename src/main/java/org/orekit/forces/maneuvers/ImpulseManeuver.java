/* Copyright 2002-2008 CS Communication & Systèmes
 * Licensed to CS Communication & Systèmes (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orekit.forces.maneuvers;

import org.apache.commons.math.geometry.Rotation;
import org.apache.commons.math.geometry.Vector3D;
import org.orekit.attitudes.Attitude;
import org.orekit.attitudes.LofOffset;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.orbits.EquinoctialOrbit;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.ApsideDetector;
import org.orekit.propagation.events.DateDetector;
import org.orekit.propagation.events.EventDetector;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.PVCoordinates;

/** Impulse maneuver model.
 * <p>This class implements an impulse maneuver as a discrete event
 * that can be provided to any {@link org.orekit.propagation.Propagator
 * Propagator}.</p>
 * <p>The maneuver is triggered by another underlying event. In the simple
 * cases, it may be a {@link DateDetector date event}, but it can also
 * be a more elaborate {@link ApsideDetector apside event} for apogee
 * maneuvers for example.</p>
 * <p>The maneuver is defined by a single velocity increment in satellite
 * frame. The current attitude of the spacecraft, defined by the current
 * spacecraft state, will be used to compute the velocity direction in
 * inertial frame. A typical case for tangential maneuvers is to use a
 * {@link LofOffset LOF aligned} attitude law for state propagation and a
 * velocity increment along the +X satellite axis.</p>
 * @see org.orekit.propagation.Propagator#addEventDetector(EventDetector)
 * @author Luc Maisonobe
 * @version $Revision$ $Date$
 */
public class ImpulseManeuver implements EventDetector {

    /** Serializable UID. */
    private static final long serialVersionUID = -7150871329986590368L;

    /** Triggering event. */
    private final EventDetector trigger;

    /** Velocity increment in satellite frame. */
    private final Vector3D deltaVSat;

    /** Engine exhaust velocity. */
    private final double vExhaust;

    /** Build a new instance.
     * @param trigger triggering event
     * @param deltaVSat velocity increment in satellite frame
     * @param isp engine specific impulse (s)
     */
    public ImpulseManeuver(final EventDetector trigger, final Vector3D deltaVSat,
                           final double isp) {
        this.trigger   = trigger;
        this.deltaVSat = deltaVSat;
        this.vExhaust  = ConstantThrustManeuver.G0 * isp;
    }

    /** {@inheritDoc} */
    public double getMaxCheckInterval() {
        return trigger.getMaxCheckInterval();
    }

    /** {@inheritDoc} */
    public int getMaxIterationCount() {
        return trigger.getMaxIterationCount();
    }

    /** {@inheritDoc} */
    public double getThreshold() {
        return trigger.getThreshold();
    }

    /** {@inheritDoc} */
    public int eventOccurred(SpacecraftState s) throws OrekitException {
        return RESET_STATE;
    }

    /** {@inheritDoc} */
    public double g(SpacecraftState s) throws OrekitException {
        return trigger.g(s);
    }

    /** {@inheritDoc} */
    public SpacecraftState resetState(SpacecraftState oldState)
            throws OrekitException {

        final Frame j2000       = Frame.getJ2000();
        final AbsoluteDate date = oldState.getDate();
        final Attitude attitude = oldState.getAttitude();

        // convert velocity increment in J2000 frame
        final Rotation refToJ2000 =
            attitude.getReferenceFrame().getTransformTo(j2000, date).getRotation();
        final Rotation satToJ2000 = refToJ2000.applyTo(attitude.getRotation().revert());
        final Vector3D deltaV = satToJ2000.applyTo(deltaVSat);

        // apply increment to position/velocity
        final PVCoordinates oldPV = oldState.getPVCoordinates(j2000);
        final PVCoordinates newPV = new PVCoordinates(oldPV.getPosition(),
                                                      oldPV.getVelocity().add(deltaV));

        // compute new mass
        final double newMass = oldState.getMass() * Math.exp(-deltaV.getNorm() / vExhaust);

        // pack everything in a new state
        return new SpacecraftState(new EquinoctialOrbit(newPV, j2000, date, oldState.getMu()),
                                   attitude, newMass);

    }

}
