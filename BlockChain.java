import java.util.HashMap;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

// Main class example at https://github.com/keskival/cryptocurrency-course-materials/blob/master/assignment3/Main.java

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    

    private HashMap<byte[], Block> blockChain;
    private TransactionPool txPool = new TransactionPool();
    private UTXOPool utxoPool = new UTXOPool();
    private TxHandler txHandler = new TxHandler(this.utxoPool);
    private int currentHeight = 0;
    private Block maxHeightBlock;


    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // Create a new blockchain including the genesis block
    	this.blockChain = new HashMap<byte[], Block>();
    	this.blockChain.put(genesisBlock.getHash(), genesisBlock);
    	this.maxHeightBlock = genesisBlock;
    	this.currentHeight++;
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
    	return this.maxHeightBlock;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
    	return this.utxoPool;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return this.txPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
    	// One of the conditions: If you receive a block which claims to be a genesis block (parent is a null hash) in the addBlock(Block b) function, you can return false.
    	if (block.getPrevBlockHash() == null) {
    		return false;
    	}else{
    		
    		// Check all of the transactions are valid
    		for (Transaction tx : block.getTransactions()) {
    			if (!this.txHandler.isValidTx(tx)) {
    				return false;
    			}
			}
    		
    		// Add the block
    		this.blockChain.put(block.getHash(), block);
    		
    		// This now becomes the maxheightblock
    		this.maxHeightBlock = block;    		
    		return true;
    	}
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        this.txPool.addTransaction(tx);
    }
}