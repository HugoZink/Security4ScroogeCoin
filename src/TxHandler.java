/**
 * Uitwerking: Hugo Zink
 */

import java.util.ArrayList;

public class TxHandler {

    private UTXOPool uPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.uPool = utxoPool;
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
    public boolean isValidTx(Transaction tx)
    {
        // IMPLEMENT THIS
        ArrayList<UTXO> seen = new ArrayList<UTXO>();

        double inputsSum = 0;
        double outputsSum = 0;

        int index = 0;

        for(Transaction.Input in: tx.getInputs())
        {
            UTXO UTXOToCheck = new UTXO(in.prevTxHash, in.outputIndex);

            //Has not been claimed yet (condition 3)
            if(seen.contains(UTXOToCheck))
            {
                return false;
            }

            //Add input to seen inputs
            seen.add(UTXOToCheck);

            //Is in UTXO pool (condition 1)
            if(!uPool.contains(UTXOToCheck))
            {
                return false;
            }

            inputsSum += uPool.getTxOutput(UTXOToCheck).value;

            //Has valid signature (condition 2)
            if(!Crypto.verifySignature(uPool.getTxOutput(UTXOToCheck).address, tx.getRawDataToSign(index), in.signature))
            {
                return false;
            }

            index++;
        }

        for(Transaction.Output out: tx.getOutputs())
        {
            //Output value is not negative (condition 4)
            if(out.value < 0)
            {
                return false;
            }
            outputsSum += out.value;
        }

        //Sum of inputs is greater than or equal to outputs (condition 5)
        if(outputsSum > inputsSum)
        {
            return false;
        }

        //If this statement is reached, all 5 conditions are satisfied.
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs)
    {
        // IMPLEMENT THIS
        ArrayList<Transaction> validTransactions = new ArrayList<Transaction>();

        for(Transaction transaction: possibleTxs)
        {
            //Skip invalid transactions
            if(!this.isValidTx(transaction))
            {
                continue;
            }

            // Remove old UTXOs from Pool
            for (Transaction.Input in : transaction.getInputs())
            {
                UTXO oldUTXO = new UTXO(in.prevTxHash, in.outputIndex);
                uPool.removeUTXO(oldUTXO);
            }

            // Add new UTXOs to Pool
            for(int i = 0; i < transaction.getOutputs().size(); i++)
            {
                UTXO newUTXO = new UTXO(transaction.getHash(), i);
                uPool.addUTXO(newUTXO, transaction.getOutputs().get(i));
            }

            validTransactions.add(transaction);
        }

        //Convert ArrayList to array and return it.
        Transaction[] returnArr = new Transaction[validTransactions.size()];
        return validTransactions.toArray(returnArr);
    }

}
