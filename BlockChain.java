import java.util.HashMap;
import java.util.logging.Handler;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

// Main class example at https://github.com/keskival/cryptocurrency-course-materials/blob/master/assignment3/Main.java

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
	
	// Internal class to add extra data regarding height - this is basically and ID!
	class MetaBlock{
		public Block block;
		public MetaBlock previous_metablock;
		public int height;
		// In this version, we put the pool here!
		public UTXOPool utxoPool;
		
		public MetaBlock(Block block, MetaBlock previous_metablock, UTXOPool utxoPool){
			this.block = block;
			this.previous_metablock = previous_metablock;
			this.utxoPool = utxoPool;
			//The height will be 1 for the genesis block, or the height of the previous one + 1 otherwise
			if (previous_metablock==null){
				this.height = 1;
			}else{
				this.height = previous_metablock.height + 1;
			}
			
		}
	}
	
	
    private HashMap<byte[], MetaBlock> blockChain; // This now becomes a list of Metablocks!
    private TransactionPool txPool = new TransactionPool();
    
    //private TxHandler txHandler = new TxHandler(this.utxoPool); - old idea, see where to instantiate now
    private Block maxHeightBlock;
    private MetaBlock maxHeightMetaBlock;


    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // Create a new blockchain  of METABLOCKS! including the genesis block
    	this.blockChain = new HashMap<byte[], MetaBlock>();
    	
    	// Create a metablock for the genesis block and a new utxoPool
    	UTXOPool genesis_utxo_pool =  new UTXOPool();
    	MetaBlock genesis_metablock = new MetaBlock(genesisBlock, null, genesis_utxo_pool);
    	
    	// Add it to the blockchain
    	this.blockChain.put(genesisBlock.getHash(), genesis_metablock);
    	
    	//Add coinbase for genesis
         Transaction coinbase = genesisBlock.getCoinbase();
         for (int i = 0; i < coinbase.numOutputs(); i++) {
             Transaction.Output out = coinbase.getOutput(i);
             UTXO utxo = new UTXO(coinbase.getHash(), i);
             genesis_utxo_pool.addUTXO(utxo, out);
         }
    	
    	this.maxHeightBlock = genesisBlock;
    	this.maxHeightMetaBlock = genesis_metablock;
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
    	return this.maxHeightBlock;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
    	return this.maxHeightMetaBlock.utxoPool;
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
    		// Check parent block
    		MetaBlock previous_metablock = this.blockChain.get(block.getPrevBlockHash());
    		if (previous_metablock == null){
    			return false;
    		}else{
    			
    			// We now need to instatiate a handler
    			TxHandler txHandler = new TxHandler(previous_metablock.utxoPool);
    			
	    		// Check all of the transactions are valid
	    		for (Transaction tx : block.getTransactions()) {
	    			if (!txHandler.isValidTx(tx)) {
	    				return false;
	    			}
    			}
	    		
	    		
	    		//Checking the height. We need to consider the CUT_OFF_AGE
	    		if (previous_metablock.height + 1 <= this.maxHeightMetaBlock.height - CUT_OFF_AGE){
	    			return false;
	    		}
	    		
	    		// Add the metablock instead... this is why we needed to add the extra method
	    		MetaBlock new_metablock =  new MetaBlock(block, previous_metablock, txHandler.getUTXOPool());
	    		
	    		this.blockChain.put(block.getHash(), new_metablock);
	    		
	        	//Add coinbase for block
	            Transaction coinbase = block.getCoinbase();
	            for (int i = 0; i < coinbase.numOutputs(); i++) {
	                Transaction.Output out = coinbase.getOutput(i);
	                UTXO utxo = new UTXO(coinbase.getHash(), i);
	                txHandler.getUTXOPool().addUTXO(utxo, out);
	            }

	    		// This now becomes the maxheightblock + metablock
	    		this.maxHeightBlock = block;
	    		this.maxHeightMetaBlock = new_metablock;
	    		return true;
    		}
    	}
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        this.txPool.addTransaction(tx);
    }
}