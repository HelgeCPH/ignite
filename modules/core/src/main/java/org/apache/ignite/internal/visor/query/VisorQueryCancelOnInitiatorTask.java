/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.visor.query;

import java.util.Collections;
import java.util.List;
import org.apache.ignite.IgniteException;
import org.apache.ignite.compute.ComputeJobResult;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.processors.task.GridInternal;
import org.apache.ignite.internal.processors.task.GridVisorManagementTask;
import org.apache.ignite.internal.visor.VisorJob;
import org.apache.ignite.internal.visor.VisorOneNodeTask;
import org.apache.ignite.lang.IgniteClosure;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.jetbrains.annotations.Nullable;

/**
 * Task to cancel queries on initiator node.
 */
@GridInternal
@GridVisorManagementTask
public class VisorQueryCancelOnInitiatorTask extends VisorOneNodeTask<VisorQueryCancelOnInitiatorTaskArg, Void> {
    /** */
    private static final long serialVersionUID = 0L;

    /** {@inheritDoc} */
    @Override protected VisorCancelQueryOnInitiatorJob job(VisorQueryCancelOnInitiatorTaskArg arg) {
        return new VisorCancelQueryOnInitiatorJob(arg, debug);
    }

    /** {@inheritDoc} */
    @Nullable @Override protected Void reduce0(List<ComputeJobResult> results) throws IgniteException {
        return null;
    }

    /** Job to cancel query on node. */
    private static class VisorCancelQueryOnInitiatorJob extends VisorJob<VisorQueryCancelOnInitiatorTaskArg, Void> {
        /** */
        private static final long serialVersionUID = 0L;

        /**
         * Create job with specified argument.
         *
         * @param arg Job argument.
         * @param debug Flag indicating whether debug information should be printed into node log.
         */
        protected VisorCancelQueryOnInitiatorJob(VisorQueryCancelOnInitiatorTaskArg arg, boolean debug) {
            super(arg, debug);
        }

        /** {@inheritDoc} */
        @Override protected Void run(VisorQueryCancelOnInitiatorTaskArg arg) throws IgniteException {
            ignite.compute(ignite.cluster().forNodeId(arg.getNodeId())).broadcast(new CancelQueryOnInitiatoClosure(),
                arg.getQueryId());

            return null;
        }
    }

    /**
     * Cancel query on initiator closure.
     */
    private static class CancelQueryOnInitiatoClosure implements IgniteClosure<Long, Void> {
        /** */
        private static final long serialVersionUID = 0L;

        /** Auto-injected grid instance. */
        @IgniteInstanceResource
        private transient IgniteEx ignite;

        /** {@inheritDoc} */
        @Override public Void apply(Long qryId) {
            ignite.context().query().cancelQueries(Collections.singleton(qryId));

            return null;
        }
    }
}
