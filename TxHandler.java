import java.util.ArrayList;

//See https://github.com/drozas/cryptocurrency-course-materials for tests
//See https://www.coursera.org/learn/cryptocurrency/programming/KOo3V/scrooge-coin/discussions for previous discussions

public class TxHandler {
	UTXOPool my_pool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
    	my_pool = new UTXOPool(utxoPool);
    	
    }
    
    public UTXOPool getUTXOPool(){
    	return my_pool;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
   	
    	UTXOPool duplicated_utxo_pool = new UTXOPool();
    	
    	
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////
    	//Extra condition: check is not null!
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////

    	if (tx == null) {
    		return false;
    	}
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////
 	
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////
        // First condition - all output claimed by tx are in current utxopool. 
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////

        for (Transaction.Input input: tx.getInputs()) {
        	//Creo utxo temporal con el hash de la transaccion anterior y su output
        	UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            // Si no esta en el pool, lanzo error. Esto seria equivalente a si no esta en la cadena de bloques?
            if (!my_pool.contains(utxo)) {
                return false;
            }

        }
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////

        
        
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////    	
    	//Second condition the signatures on each input of {@code tx} are valid
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////

        int i = 0;
        for (Transaction.Input input: tx.getInputs()) {
        	//Creo utxo temporal con el hash de la transaccion anterior y su output
        	UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
        	if (!Crypto.verifySignature(my_pool.getTxOutput(utxo).address, tx.getRawDataToSign(i), input.signature)){
        		return false;
        	}
        	i++;
        }
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////

    	/////////////////////////////////////////////////////////////////////////////////////////////////////////
    	//Fourth condition:  all of {@code tx}s output values are non-negative
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////
    	
        for (Transaction.Output o : tx.getOutputs()) {
			if (o.value<0){
				return false;
			}
		}
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////

    	
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////
    	//Fifth condition:  (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output values; and false otherwise.
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////

    	double total_value_outputs = 0;
		double total_value_inputs = 0;

    	//Calculate total value outputs in this transaction
		for (Transaction.Output o : tx.getOutputs()) {
    		total_value_outputs += o.value;
		}
    	
    	//Calculate total value inputs going through previous transactions outputs
		for (Transaction.Input input: tx.getInputs()) {
			//Creo utxo temporal con el hash de la transaccion anterior y su output
        	UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
        	total_value_inputs += my_pool.getTxOutput(utxo).value;
        	
        }
    	
		//Carry out comparison for (5)
    	if (total_value_inputs<total_value_outputs){
    		return false;
    	}
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////
    	
    	
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////
    	//Third condition UTXO is claimed multiple times by {@code tx}
    	//We do this at the end because we need to add it only if it passes all of the previous conditions
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////
    	
    	//Check if utxo in our duplicated pool checker
		for (Transaction.Input input: tx.getInputs()) {
			//Creo utxo temporal con el hash de la transaccion anterior y su output
        	UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
        	if (duplicated_utxo_pool.contains(utxo)){
        		return false;
        	}else{
        		duplicated_utxo_pool.addUTXO(utxo, my_pool.getTxOutput(utxo));
        	}
        }

    	/////////////////////////////////////////////////////////////////////////////////////////////////////////

    	return true;
    }

    public Transaction[] handleTxs(Transaction[] possibleTxs) {

    	Transaction[] my_valid_txs = new Transaction[10000]; // This would be the maximum amount of transactions in the block to mine?
    	int valid_txs_index = 0;
    	
    	//Inspect whole array of proposed transactions
    	for (Transaction tx: possibleTxs) {
    		
    		if (isValidTx(tx)){
    			// Si la tx es valida, la anado a la lista de validas
        		my_valid_txs[valid_txs_index] = tx;
        		valid_txs_index++;
        		// Y actualizo la pool con los outputs de cada input
        		
                // Hago un primer for para sacar todos los inputs que me he gastado del pool (que es el estado general)
        		for (Transaction.Input input: tx.getInputs()) {
        			//Creo utxo temporal con el hash de la transaccion anterior y su output
                	UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                	my_pool.removeUTXO(utxo);
                }
                
                // Hago un segundo for para anadir los outputs ya validados que se pueden gastar
        		int output_index = 0;
                for (Transaction.Output output: tx.getOutputs()) {
                	UTXO utxo = new UTXO(tx.getHash(), output_index);
                	my_pool.addUTXO(utxo,output);
                	output_index++;
                }
    		}

    	}
    	
    	return my_valid_txs;
    }

}
