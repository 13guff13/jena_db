package org.apache.jena.sparql.util;

import static org.apache.jena.atlas.iterator.Iter.count;
import static org.apache.jena.atlas.iterator.Iter.map;
import static org.apache.jena.ext.com.google.common.collect.Iterators.concat;
import static org.apache.jena.query.ReadWrite.WRITE;
import static org.apache.jena.sparql.core.Quad.ANY;
import static org.apache.jena.sparql.core.Quad.defaultGraphIRI;
import static org.apache.jena.sparql.util.graph.GraphUtils.triples2quads;

import java.util.Iterator;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

public abstract class ViewDatasetGraph extends Pair.OfSameType<DatasetGraph> implements DatasetGraph {

	private final Context context;

	private final Lock lock;

	public ViewDatasetGraph(DatasetGraph left, DatasetGraph right) {
		this(left, right, Context.emptyContext);
	}

	public ViewDatasetGraph(DatasetGraph left, DatasetGraph right, Context c) {
		super(left, right);
		this.context = c;
		this.lock = new PairLock(left.getLock(), right.getLock());
	}

	private void noMutation() {
		throw new UnsupportedOperationException("Views do not allow mutation!");
	}

	@Override
	public void commit() {
		noMutation();
	}


	@Override
	public void begin(ReadWrite readWrite) {
		if (readWrite.equals(WRITE)) noMutation();
		forEach(dsg -> dsg.begin(readWrite));
	}

	@Override
	public void abort() {
		noMutation();
	}

	@Override
	public void end() {
		forEach(DatasetGraph::end);
	}

	@Override
	public boolean isInTransaction() {
		return either(DatasetGraph::isInTransaction);
	}

	@Override
	public void setDefaultGraph(Graph g) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addGraph(Node graphName, Graph graph) {
		noMutation();
	}

	@Override
	public void removeGraph(Node graphName) {
		noMutation();
	}

	@Override
	public void add(Quad quad) {
		noMutation();
	}

	@Override
	public void delete(Quad quad) {
		noMutation();
	}

	@Override
	public void add(Node g, Node s, Node p, Node o) {
		noMutation();
	}

	@Override
	public void delete(Node g, Node s, Node p, Node o) {
		noMutation();
	}

	@Override
	public void deleteAny(Node g, Node s, Node p, Node o) {
		noMutation();
	}

	@Override
	public void clear() {
		noMutation();
	}

	@Override
	public Iterator<Quad> find() {
		return find(ANY);
	}

	@Override
	public Iterator<Quad> find(Quad q) {
		return find(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
	}

	@Override
	public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
		return g.isConcrete()
				? findInOneGraph(g, s, p, o)
				: concat(findNG(null, s, p, o), findInOneGraph(defaultGraphIRI, s, p, o));
	}

	@Override
	public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
		return g.isConcrete()
				? findInOneGraph(g, s, p, o)
				: concat(map(listGraphNodes(), gn -> findInOneGraph(gn, s, p, o)));
	}

	protected Iterator<Quad> findInOneGraph(Node g, Node s, Node p, Node o) {
		return triples2quads(g, getGraph(g).find(s, p, o));
	}

	@Override
	public Graph getUnionGraph() {
		return new MultiUnion(map(listGraphNodes(), this::getGraph));
	}

	@Override
	public boolean contains(Quad q) {
		return contains(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
	}

	@Override
	public Lock getLock() {
		return lock;
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public void close() {
	}

	@Override
	public boolean supportsTransactions() {
		return both(DatasetGraph::supportsTransactions);
	}

	@Override
	public boolean supportsTransactionAbort() {
		return false;
	}

	@Override
	public long size() {
		return count(listGraphNodes());
	}

	@Override
	public boolean isEmpty() {
		return listGraphNodes().hasNext();
	}

	private static class PairLock extends Pair.OfSameType<Lock> implements Lock {

		public PairLock(Lock left, Lock right) {
			super(left, right);
		}

		@Override
		public void enterCriticalSection(boolean readLockRequested) {
			forEach(lock -> lock.enterCriticalSection(readLockRequested));
		}

		@Override
		public void leaveCriticalSection() {
			forEach(Lock::leaveCriticalSection);
		}
	}
}
