package edu.rit.se.design.arcode.fspec2recom;

import edu.rit.se.design.arcode.fspecminer.fspec.FSpec;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpecEdge;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpecEdgeType;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class SubFSpec extends FSpec {
    FSpec fSpec;

    public SubFSpec(FSpec fSpec) {
        super( fSpec.getFramework() );
        this.fSpec = fSpec;
        removeNode( getRoot() );
        addNode( fSpec.getRoot() );
    }

    public FSpec getfSpec() {
        return fSpec;
    }

    @Override
    protected FSpecEdgeType getDefaultEdgeType() {
        return FSpecEdgeType.EXPLICIT_DATA_DEP;
    }

    @Override
    protected FSpecEdge getDefaultEdgeInfo(FSpecEdgeType edgeType) {
        return new FSpecEdge("");
    }

    @Override
    public String getTitle() {
        return "SubFSpec of " + fSpec.getTitle();
    }
}
